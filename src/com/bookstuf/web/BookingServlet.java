package com.bookstuf.web;

import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;

import static com.googlecode.objectify.ObjectifyService.ofy;

import com.bookstuf.GsonHelper;
import com.bookstuf.appengine.HandleToProfessionalInformationKeyMemcacheable;
import com.bookstuf.appengine.NotLoggedInException;
import com.bookstuf.appengine.RetryHelper;
import com.bookstuf.appengine.StripeApi;
import com.bookstuf.appengine.UserManager;
import com.bookstuf.datastore.Booking;
import com.bookstuf.datastore.ConsumerInformation;
import com.bookstuf.datastore.ConsumerDailyAgenda;
import com.bookstuf.datastore.DailyAgenda;
import com.bookstuf.datastore.PaymentMethod;
import com.bookstuf.datastore.PaymentStatus;
import com.bookstuf.datastore.ProfessionalInformation;
import com.bookstuf.datastore.Service;
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.identitytoolkit.GitkitClientException;
import com.google.identitytoolkit.GitkitUser;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Result;
import com.googlecode.objectify.Work;
import com.stripe.exception.StripeException;
import com.stripe.model.Card;
import com.stripe.model.Customer;

@Singleton
@SuppressWarnings("serial")
public class BookingServlet extends RpcServlet {
	private final Logger logger;
	private final UserManager userService;
	private final GsonHelper gsonHelper;
	private final Provider<GitkitUser> gitkitUser;
	private final RetryHelper retryHelper;
	private final StripeApi stripe;
	private final Provider<ListeningExecutorService> execService;
	
	@Inject BookingServlet(
		final Logger logger,
		final UserManager userService,
		final GsonHelper gsonHelper,
		final Provider<GitkitUser> gitkitUser,
		final RetryHelper retryHelper,
		final Provider<ListeningExecutorService> execService,
		final StripeApi stripe
	) {
		this.logger = logger;
		this.userService = userService;
		this.gsonHelper = gsonHelper;
		this.gitkitUser = gitkitUser;
		this.retryHelper = retryHelper;
		this.execService = execService;
		this.stripe = stripe;
	}

	@Default
	private void notFound(final HttpServletResponse response) {
		response.setStatus(404);
	}
	
	public static class BookingRequest {
		public String professionalUserId;
		public Service service;
		public LocalDate date;
		public LocalTime startTime;
		
		public PaymentMethod paymentMethod;
		
		public boolean isNewStripeCustomer;
		public String stripeToken;
	}
	
	private void handleCashPaymentMethod(
		final Booking booking
	) {
		booking.setPaymentStatus(PaymentStatus.PENDING);
		booking.setPaymentMethod(PaymentMethod.CASH);
	}
	
	public static class ExpiredCardException extends Exception {
		public final int expYear;
		public final int expMonth;
		
		public ExpiredCardException(
			final int expYear, 
			final int expMonth
		) {
			this.expYear = expYear;
			this.expMonth = expMonth;
		}
	}
	
	private void handleStripeCardPaymentMethod(
		final Booking booking, 
		final BookingRequest request
	) throws Exception {
		// generate idempotent keys for stripe requests
		final String customerCreateIdemKey = UUID.randomUUID().toString();
		final String customerRetrieveIdemKey = UUID.randomUUID().toString();
		
		retryHelper.execute(10000, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				ofy().transactNew(0, new Work<Void>() {
					@Override
					public Void run() {
						try {
							final Result<ConsumerInformation> consumerInformationResult =
								userService.getCurrentConsumerInformation();
							
							booking.setPaymentStatus(PaymentStatus.PENDING);
							booking.setPaymentMethod(PaymentMethod.STRIPE_CARD);
							
							// create/retrieve customer from stripe
							final Customer stripeCustomer = request.isNewStripeCustomer ?
								stripe.customer().create().source(request.stripeToken).idempotencyKey(customerCreateIdemKey).get() :
								stripe.customer().retrieve(consumerInformationResult.now().getStripeCustomerId()).idempotencyKey(customerRetrieveIdemKey).get();
								
							// save new customer id to the datastore
							if (request.isNewStripeCustomer) {
								final ConsumerInformation consumerInformation =
									consumerInformationResult.now();
								
								consumerInformation.setStripeCustomerId(stripeCustomer.getId());
			
								ofy().save().entity(consumerInformation);
							}
							
							// make sure default card won't expire before the appointment
							final Card defaultCard = 
								getDefaultCard(stripeCustomer);
							
							if (cardWillBeExpired(defaultCard, request.date)) {
								throw new ExpiredCardException(
									defaultCard.getExpYear().intValue(), 
									defaultCard.getExpMonth().intValue());
							}
							
							// set stripe billing information into the booking
							booking.setStripeCustomerId(stripeCustomer.getId());
							booking.setStripeCardId(defaultCard.getId());
							
							return null;
							
						} catch (final Exception e) {
							throw new RuntimeException(e);
						}
					} // run
				}); // ofy().transactionNew(Work)
				
				return null;
			} // call
		}); // retryHelper.execute(Callable)
	}
	

	private ListenableFuture<Void> handlePaymentMethod(
		final Booking booking, 
		final BookingRequest request
	) {
		return execService.get().submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {		
				if (request.paymentMethod == PaymentMethod.STRIPE_CARD) {
					handleStripeCardPaymentMethod(booking, request);
					
				} else {
					handleCashPaymentMethod(booking);
				}
				
				return null;
			}
		});
	}
	
	// TODO: need servlet method to retrieve card summary information for user to select which card to use
	// TODO: consolidate error reporting format
	// TODO: need to create handlers for all individual StripeException subclasses
	// TODO: initiate charge request for user in cron job, handle cash-only transactions and add charge to professional account
	@Publish
	private String book(
		@RequestBody final BookingRequest request
	) throws 
		Exception 
	{	
		final Booking requestedBooking =
			new Booking();
		
		
		// payment can be handled in parallel.  it could take a long time for
		// roundtrips to both datastore and stripe's servers if needed.
		final ListenableFuture<Void> paymentMethodStatus =
			handlePaymentMethod(requestedBooking, request);

		
		// need to prepare as much information outside of transaction as 
		// possible to ensure transaction can execute as fast as possible
		final String consumerUserId =
			gitkitUser.get().getLocalId();
		
		final Key<ConsumerInformation> consumerKey = 
			Key.create(ConsumerInformation.class, consumerUserId);
		
		final Key<ProfessionalInformation> professionalKey = 
			Key.create(ProfessionalInformation.class, request.professionalUserId);
		
		
		// fill in booking information
		requestedBooking.setProfessional(professionalKey);
		requestedBooking.setConsumer(consumerKey);
		requestedBooking.setService(request.service);
		requestedBooking.setStartTime(request.startTime);

		
		return 
			retryHelper.execute(10000, new Callable<String>() {
				@Override
				public String call() throws Exception {
					return ofy().transactNew(0, new Work<String>() {
						@Override
						public String run() {
							try {
								// initiate fetch for daily agendas
								final Result<DailyAgenda> professionalDailyAgendaResult =
									getProfessionalDailyAgenda(request.professionalUserId, request.date);
								
								final Result<ConsumerDailyAgenda> consumerDailyAgendaResult =
									getConsumerDailyAgenda(consumerKey, consumerUserId, request.date);
				
								
								// wait for the daily agendas to be ready
								final DailyAgenda professionalDailyAgenda =
									professionalDailyAgendaResult.now();
								
								final ConsumerDailyAgenda consumerDailyAgenda =
									consumerDailyAgendaResult.now();
								
								
								// need to wait for payment method to finish updating the booking 
								// with payment information and give it the opportunity to throw 
								// an exception and cancel the booking
								paymentMethodStatus.get();
								
								
								// see if we can make the booking...
								if (
									professionalDailyAgenda.canAdd(requestedBooking) &&
									consumerDailyAgenda.canAdd(requestedBooking)
								) {
									// ...make it if we can...
									professionalDailyAgenda.add(requestedBooking);
									consumerDailyAgenda.add(requestedBooking);
									
									// professional daily agenda is used to manage booking payment
									
									// save agendas back to the datastore
									ofy().save().entity(professionalDailyAgenda);
									ofy().save().entity(consumerDailyAgenda);
									
									// ...and report success
									return "{\"success\": true}";
									
								} else {
									ofy().getTransaction().rollbackAsync();
									
									// ...report failure if we can't
									return "{\"alreadyBooked\": true}";
								}
							} catch(final Exception e) {
								throw new RuntimeException(e);
							}
						}
					});
				}
				
			});
		
		

	}

	private Card getDefaultCard(
		final Customer stripeCustomer
	) throws 
		StripeException 
	{
		final String defaultCardId = 
			stripeCustomer.getDefaultCard();
		
		for (final Card card : stripeCustomer.getCards().getData()) {
			if (card.getId().equals(defaultCardId)) {
				return card;
			}
		}
		
		// didn't already have the card, need to fetch it
		final Card defaultCard =
			stripe.card().retrieve(stripeCustomer, defaultCardId).get();
		
		return defaultCard;
	}
	
	private boolean cardWillBeExpired(
		final Card card,
		final LocalDate date
	) {
		return 
			card.getExpYear().intValue() < date.getYear() ||
			(
				card.getExpYear().intValue() == date.getYear() &&
				card.getExpMonth().intValue() < date.getMonthValue()
			);
	}

	private Result<DailyAgenda> getProfessionalDailyAgenda(
		final String professionalUserId, 
		final LocalDate date
	) {
		final Key<DailyAgenda> key =
			DailyAgenda.createKey(professionalUserId, date);

		return new Result<DailyAgenda>() {
			@Override
			public DailyAgenda now() {
				try {
					return ofy().load().key(key).safe();
					
				} catch (final NotFoundException e) {
					final DailyAgenda dailyAgenda =
						new DailyAgenda();
					
					dailyAgenda.setOwnerAndDate(professionalUserId, date);
					
					return dailyAgenda;
				}
			}		
		};
	}

	private Result<ConsumerDailyAgenda> getConsumerDailyAgenda(
		final Key<ConsumerInformation> consumerKey, 
		final String consumerUserId, 
		final LocalDate date
	) {
		final Key<ConsumerDailyAgenda> key =
			ConsumerDailyAgenda.createKey(consumerKey, consumerUserId, date);
		
		return new Result<ConsumerDailyAgenda>() {
			@Override
			public ConsumerDailyAgenda now() {
				try {
					return ofy().load().key(key).safe();
					
				} catch (final NotFoundException e) {
					final ConsumerDailyAgenda dailyAgenda =
						new ConsumerDailyAgenda();
					
					dailyAgenda.setOwnerAndDate(consumerUserId, date);
					
					return dailyAgenda;
				}
			}	
		};
	}

	
	
	@ExceptionHandler(ExpiredCardException.class) 
	private void handleExpiredCardException(
		final HttpServletResponse response
	) throws 
		IOException 
	{
		response.getWriter().println("{\"unableToProcessCard\": true}");
	}
	
	@ExceptionHandler(DatastoreFailureException.class) 
	private void handleDatastoreFailureException(
		final HttpServletResponse response
	) throws 
		IOException 
	{
		response.getWriter().println("{\"tryAgain\": true}");
	}

	@ExceptionHandler(ConcurrentModificationException.class) 
	private void handleConcurrentModificationException(
		final HttpServletResponse response
	) throws 
		IOException 
	{
		response.getWriter().println("{\"tryAgain\": true}");
	}

	@ExceptionHandler(StripeException.class) 
	private void handleStripeException(
		final HttpServletResponse response
	) throws 
		IOException 
	{
		response.getWriter().println("{\"unableToProcessCard\": true}");
	}

	@ExceptionHandler(NotLoggedInException.class) 
	private void handleNotLoggedInException(
		final HttpServletResponse response
	) throws 
		IOException 
	{
		response.getWriter().println("{\"notLoggedIn\": true}");
	}
	
	@ExceptionHandler(GitkitClientException.class) 
	private void handleGitkitClientException(
		final HttpServletResponse response
	) throws 
		IOException 
	{
		logger.log(Level.SEVERE, "Could not validate gitkit user.", getCurrentException());
		response.getWriter().println("{\"notLoggedIn\": true}");
	}
}
