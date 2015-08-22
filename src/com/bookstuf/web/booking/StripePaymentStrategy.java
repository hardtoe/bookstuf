package com.bookstuf.web.booking;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bookstuf.Luke;
import com.bookstuf.appengine.RetryHelper;
import com.bookstuf.appengine.StripeApi;
import com.bookstuf.appengine.UserManager;
import com.bookstuf.datastore.Booking;
import com.bookstuf.datastore.ConsumerInformation;
import com.bookstuf.datastore.PaymentMethod;
import com.bookstuf.datastore.ProfessionalPrivateInformation;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Result;
import com.googlecode.objectify.util.Closeable;
import com.stripe.exception.StripeException;
import com.stripe.model.Card;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.ExternalAccount;

public class StripePaymentStrategy extends PaymentStrategy {
	private static final Logger log = 
		Logger.getLogger(StripePaymentStrategy.class.getCanonicalName());
	
	private final BookingRequest request;
	private final Booking booking;
	private final UserManager userManager;
	private final ListeningExecutorService execService;
	private final StripeApi stripe;
	private final RetryHelper retryHelper;
	
	private final String customerRetrieveIdemKey;
	private final String cardAddIdemKey;
	private final String chargeIdemKey;
	
	/**
	 * card to charge
	 */
	private ListenableFuture<Card> cardFuture;
	
	/**
	 * professional account to credit
	 */
	private LoadResult<ProfessionalPrivateInformation> professionalPrivateInfo;
	
	public StripePaymentStrategy(
		final BookingRequest request, 
		final Booking booking,
		final UserManager userManager,
		final ListeningExecutorService execService,
		final StripeApi stripe,
		final RetryHelper retryHelper
	) {
		this.request = request;
		this.booking = booking;
		this.userManager = userManager;
		this.execService = execService;
		this.stripe = stripe;
		this.retryHelper = retryHelper;
		
		customerRetrieveIdemKey = UUID.randomUUID().toString();
		cardAddIdemKey = UUID.randomUUID().toString();
		chargeIdemKey = UUID.randomUUID().toString();
	}

	@Override
	public void prepare() {
		final Result<ConsumerInformation> consumerInformationResult =
			userManager.getCurrentConsumerInformation();

		final Key<ProfessionalPrivateInformation> professionalPrivateKey =
			Key.create(ProfessionalPrivateInformation.class, request.professionalUserId);
		
		professionalPrivateInfo =
			ofy().transactionless().load().key(professionalPrivateKey);
		
		cardFuture = execService.submit(new Callable<Card>() {
			@Override
			public Card call() throws Exception {
				final Closeable session =
					ObjectifyService.begin();
				
				Card card;
				
				try {
					card = getOrCreateCard(
						booking, 
						request, 
						consumerInformationResult);
						
				} finally {
					session.close();
				}
				
				return card;
			}
		});
	}


	private Card getOrCreateCard(
		final Booking booking, 
		final BookingRequest request,
		final Result<ConsumerInformation> consumerInformationResult
	) throws 
		Exception 
	{
		return retryHelper.transactNew(10000, new Callable<Card>() {

			@Override
			public Card call() throws Exception {
				booking.setPaymentMethod(PaymentMethod.STRIPE_CARD);
				
				final ConsumerInformation consumerInformation =
					consumerInformationResult.now();
				
				final boolean isNewStripeCustomer =
					!consumerInformation.hasStripeCustomer();

				final String customerCreateIdemKey = 
					"CreateCustomer:" + consumerInformation.getGitkitUserId();
				
				// create/retrieve customer from stripe
				final Customer stripeCustomer = 
					isNewStripeCustomer ?
						stripe.customer()
							.create()
							.source(request.stripeToken)
							.email(consumerInformation.getContactEmail())
							.metadata("bookstuf.consumer.id", consumerInformation.getGitkitUserId())
							.idempotencyKey(customerCreateIdemKey)
							.get() :
								
						stripe.customer()
							.retrieve(consumerInformationResult.now().getStripeCustomerId())
							.idempotencyKey(customerRetrieveIdemKey)
							.get();

							
				// save new customer id to the datastore
				if (isNewStripeCustomer) {
					consumerInformation.setStripeCustomerId(stripeCustomer.getId());

					ofy().save().entity(consumerInformation);
				} 
				
				final boolean addNewCardToExistingAccount =
					!isNewStripeCustomer && 
					request.addNewCard && 
					stripeCustomer.getSources().getTotalCount() < 10;
				
				final Card card = 
					addNewCardToExistingAccount ? 
						stripe.card()
							.create(stripeCustomer, request.stripeToken)
							.idempotencyKey(cardAddIdemKey)
							.get() : 
						
						getCard(stripeCustomer, 
							isNewStripeCustomer ? 
								stripeCustomer.getDefaultSource() : 
								request.cardId);

				if (
					addNewCardToExistingAccount && 
					request.setNewCardAsDefault
				) {
					stripe.customer()
						.update(stripeCustomer)
						.defaultSource(card.getId())
						.get();
				}
				
				// set stripe billing information into the booking
				booking.setStripeCustomerId(stripeCustomer.getId());
					
				return card;	
			} // call
		}); // retryHelper.execute(Callable)
	}
	
	@Override
	public boolean isPaymentLikely() throws InterruptedException, ExecutionException {
		cardFuture.get();
		return true;
	}

	@Override
	public void execute(BigDecimal cost) throws Exception {
		final int amount = 
			cost.multiply(new BigDecimal(100)).intValue();
		
		final int stripeFee =
			((amount * 29) / 1000) + 30;
		
		final int bookstufFee =
			amount / 100;

		final String professionalStripeAccountId =
			professionalPrivateInfo.now().getStripeUserId();
		
		final Charge charge =
			stripe.charge()
				.create(amount, "usd")
				.customer(booking.getStripeCustomerId())
				.source(cardFuture.get().getId())
				.destination(professionalStripeAccountId)
				.applicationFee(stripeFee + bookstufFee)
				.metadata("bookstuf.booking.id", booking.getId()) 
				.idempotencyKey(chargeIdemKey)
				.get();
		
		booking.setStripeChargeId(charge.getId());
	}

	
	private Card getCard(
		final Customer stripeCustomer, 
		final String cardId
	) throws 
		StripeException 
	{
		for (final ExternalAccount card : stripeCustomer.getSources().getData()) {
			if (card.getId().equals(cardId)) {
				return (Card) card;
			}
		}
		
		// didn't already have the card, need to fetch it
		final Card defaultCard =
			stripe.card().retrieve(stripeCustomer, cardId).get();
		
		return defaultCard;
	}

	@Override
	public void rollbackPayment() {
		log.log(Level.WARNING, "StripePaymentSystem.rollbackPayment() - enter");
		log.log(Level.WARNING, "date = " + request.date);
		log.log(Level.WARNING, "time = " + request.startTime);
		log.log(Level.WARNING, "booking.id = " + booking.getId());
		
		try {
			retryHelper.execute(10000, new Callable<TaskHandle>() {
				@Override
				public TaskHandle call() throws Exception {
					log.log(Level.INFO, "attempting to enqueue StripeChargeCleanupTask");
					
					final Queue q = 
						QueueFactory.getQueue("booking-cleanup");
					
					return q.add(TaskOptions.Builder
						.withPayload(new StripeChargeCleanupTask(booking))
						.taskName("StripeChargeCleanupTask-" + booking.getId()));
				}});
			
		} catch (final Throwable t) {
			log.log(Level.SEVERE, "FATAL - ENQUEUE: failed to enqueue cleanup task! this booking id " + booking.getId() + " needs to be manually refunded!", t);
			
			sendAdminEmail(t);
		}
	}
	
	private void sendAdminEmail(final Throwable t) {
		Luke.email(
			"charge cleanup failure for booking id " + booking.getId(), 
			
			"Could not enqueue stripe charge cleanup for booking id " + booking.getId() + ".  \n" +
    		"This will need to be cleaned up manually in the stripe dashboard.  \n\n" +
    		"https://dashboard.stripe.com/search?query=" + booking.getId() + "\n\n",
		    		
			t);
	}
}
