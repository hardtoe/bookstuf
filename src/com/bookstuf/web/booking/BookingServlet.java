package com.bookstuf.web.booking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;

import static com.googlecode.objectify.ObjectifyService.ofy;

import com.bookstuf.appengine.NotLoggedInException;
import com.bookstuf.appengine.RetryHelper;
import com.bookstuf.appengine.StripeApi;
import com.bookstuf.appengine.UserManager;
import com.bookstuf.datastore.Availability;
import com.bookstuf.datastore.Booking;
import com.bookstuf.datastore.ConsumerInformation;
import com.bookstuf.datastore.ConsumerDailyAgenda;
import com.bookstuf.datastore.DailyAgenda;
import com.bookstuf.datastore.PaymentMethod;
import com.bookstuf.datastore.PaymentStatus;
import com.bookstuf.datastore.ProfessionalInformation;
import com.bookstuf.web.Default;
import com.bookstuf.web.ExceptionHandler;
import com.bookstuf.web.Param;
import com.bookstuf.web.Publish;
import com.bookstuf.web.RequestBody;
import com.bookstuf.web.RpcServlet;
import com.bookstuf.web.card.ExpiredCardException;
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.identitytoolkit.GitkitClientException;
import com.google.identitytoolkit.GitkitUser;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Result;
import com.stripe.exception.StripeException;
import com.stripe.model.Card;
import com.stripe.model.Customer;

@Singleton
@SuppressWarnings("serial")
public class BookingServlet extends RpcServlet {
	private final Logger logger;
	private final UserManager userService;
	private final Provider<GitkitUser> gitkitUser;
	private final RetryHelper retryHelper;
	private final StripeApi stripe;
	private final Provider<ListeningExecutorService> execService;
	
	@Inject BookingServlet(
		final Logger logger,
		final UserManager userService,
		final Provider<GitkitUser> gitkitUser,
		final RetryHelper retryHelper,
		final Provider<ListeningExecutorService> execService,
		final StripeApi stripe
	) {
		this.logger = logger;
		this.userService = userService;
		this.gitkitUser = gitkitUser;
		this.retryHelper = retryHelper;
		this.execService = execService;
		this.stripe = stripe;
	}

	@Default
	private void notFound(final HttpServletResponse response) {
		response.setStatus(404);
	}
	
	private void handleCashPaymentMethod(
		final Booking booking
	) {
		booking.setPaymentStatus(PaymentStatus.PENDING);
		booking.setPaymentMethod(PaymentMethod.CASH);
	}
	
	private void handleStripeCardPaymentMethod(
		final Booking booking, 
		final BookingRequest request
	) throws Exception {
		// generate idempotent keys for stripe requests
		final String customerCreateIdemKey = UUID.randomUUID().toString();
		final String customerRetrieveIdemKey = UUID.randomUUID().toString();
		final String cardAddIdemKey = UUID.randomUUID().toString();
		
		retryHelper.transactNew(10000, new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				final Result<ConsumerInformation> consumerInformationResult =
					userService.getCurrentConsumerInformation();
				
				booking.setPaymentStatus(PaymentStatus.PENDING);
				booking.setPaymentMethod(PaymentMethod.STRIPE_CARD);
				
				
				// create/retrieve customer from stripe
				final Customer stripeCustomer = request.isNewStripeCustomer ?
					stripe.customer()
						.create()
						.source(request.stripeToken)
						.idempotencyKey(customerCreateIdemKey)
						.get() :
							
					stripe.customer()
						.retrieve(consumerInformationResult.now().getStripeCustomerId())
						.idempotencyKey(customerRetrieveIdemKey)
						.get();
					
						
				// save new customer id to the datastore
				if (request.isNewStripeCustomer) {
					final ConsumerInformation consumerInformation =
						consumerInformationResult.now();
					
					consumerInformation.setStripeCustomerId(stripeCustomer.getId());

					ofy().save().entity(consumerInformation);
				} 
				
				final boolean addNewCardToExistingAccount =
					!request.isNewStripeCustomer && 
					request.addNewCard && 
					stripeCustomer.getCards().getTotalCount() < 10;
				
				final Card card = 
					 addNewCardToExistingAccount ? 
						stripe.card()
							.create(stripeCustomer, request.stripeToken)
							.idempotencyKey(cardAddIdemKey)
							.get() : 
						
						getCard(stripeCustomer, request.cardId);

				if (
					addNewCardToExistingAccount && 
					request.setNewCardAsDefault
				) {
					stripe.customer()
						.update(stripeCustomer)
						.source(card.getId()).get();
				}
							
				// make sure the card won't expire before the appointment
				if (cardWillBeExpired(card, request.date)) {
					throw new ExpiredCardException(
						card.getExpYear().intValue(), 
						card.getExpMonth().intValue());
				}
				
				
				// set stripe billing information into the booking
				booking.setStripeCustomerId(stripeCustomer.getId());
				booking.setStripeCardId(card.getId());
				
				
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
	
	// TODO: consolidate error reporting format
	// TODO: need to create handlers for all individual StripeException subclasses
	
	// TODO: initiate charge request for user in cron job, handle cash-only transactions and add charge to professional account
	
	// TODO: send confirmation email to professional
	// TODO: send confirmation email to consumer
	
	// TODO: need to implement card deletion flow
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
		
		return retryHelper.transactNew(10000, new Callable<String>() {
			@Override
			public String call() throws Exception {
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
					
					// save daily agendas back to the datastore
					ofy().save().entity(professionalDailyAgenda);
					ofy().save().entity(consumerDailyAgenda);
					
					// ...and report success
					return "{\"success\": true}";
					
				} else {
					ofy().getTransaction().rollbackAsync();
					
					// ...report failure if we can't
					return "{\"alreadyBooked\": true}";
				}
			}
		});
	}

	private Card getCard(
		final Customer stripeCustomer, 
		final String cardId
	) throws 
		StripeException 
	{
		for (final Card card : stripeCustomer.getCards().getData()) {
			if (card.getId().equals(cardId)) {
				return card;
			}
		}
		
		// didn't already have the card, need to fetch it
		final Card defaultCard =
			stripe.card().retrieve(stripeCustomer, cardId).get();
		
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

	private Map<Key<DailyAgenda>, DailyAgenda> getProfessionalDailyAgenda(
		final String professionalUserId, 
		final LocalDate startDate,
		final int numDays
	) {
		final ArrayList<Key<DailyAgenda>> keys =
			new ArrayList<>();
		
		for (int i = 0; i < numDays; i++) {
			final LocalDate date =
				startDate.plusDays(i);

			final Key<DailyAgenda> key =
				DailyAgenda.createKey(professionalUserId, date);
			
			keys.add(key);
		}
		
		return ofy().load().keys(keys);
	}
	
	private Map<Key<ConsumerDailyAgenda>, ConsumerDailyAgenda> getConsumerDailyAgenda(
		final String consumerUserId, 
		final LocalDate startDate,
		final int numDays
	) {
		final ArrayList<Key<ConsumerDailyAgenda>> keys =
			new ArrayList<>();
		
		for (int i = 0; i < numDays; i++) {
			final LocalDate date =
				startDate.plusDays(i);

			final Key<ConsumerDailyAgenda> key =
				ConsumerDailyAgenda.createKey(Key.create(ConsumerInformation.class, consumerUserId), consumerUserId, date);
			
			keys.add(key);
		}
		
		return ofy().load().keys(keys);
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


	
	@Publish
	private List<PublicDailyAvailability> availability(
		@Param("professionalUserId") final String professionalUserId
	) {
		// TODO: change start date to the earliest date allowed for booking by professional
		final LocalDate startDate =
			LocalDate.now();
		
		final int NUM_DAYS = 14;
		
		// initiate fetch for daily agendas
		final Map<Key<DailyAgenda>, DailyAgenda> professionalDailyAgendaResult =
			getProfessionalDailyAgenda(professionalUserId, startDate, NUM_DAYS);

		final String consumerUserId =
			gitkitUser.get().getLocalId();
		
		final Map<Key<ConsumerDailyAgenda>, ConsumerDailyAgenda> consumerDailyAgendaResult =
				getConsumerDailyAgenda(consumerUserId, startDate, NUM_DAYS);

		final Key<ProfessionalInformation> professionalKey = 
			Key.create(ProfessionalInformation.class, professionalUserId);
		
		final LoadResult<ProfessionalInformation> professionalInfoResult =
			ofy().load().key(professionalKey);
		
		
		final ArrayList<PublicDailyAvailability> result =
			new ArrayList<>();
		
		final LinkedList<Availability> availability =
			professionalInfoResult.safe().getAvailability();
		
		final Map<DayOfWeek, TreeMap<LocalTime, Availability>> dailyAvailability =
			sortAvailability(availability);


		LocalDate date = startDate;
		
		final Iterator<Entry<Key<DailyAgenda>, DailyAgenda>> professionalAgendaIterator =
			professionalDailyAgendaResult.entrySet().iterator();
		
		final Iterator<Entry<Key<ConsumerDailyAgenda>, ConsumerDailyAgenda>> consumerAgendaIterator =
			consumerDailyAgendaResult.entrySet().iterator();
		
		while (
			professionalAgendaIterator.hasNext() && 
			consumerAgendaIterator.hasNext()
		) {
			final DayOfWeek dayOfWeek =
				date.getDayOfWeek();
			
			final PublicDailyAvailability publicDailyAvailability =
				new PublicDailyAvailability(
					date, 
					next(consumerAgendaIterator), 
					next(professionalAgendaIterator), 
					dailyAvailability.get(dayOfWeek));
			
			result.add(publicDailyAvailability);
			
			date = date.plusDays(1);
		}
		
		return result;
	}
	
	private Map<DayOfWeek, TreeMap<LocalTime, Availability>> sortAvailability(
		final LinkedList<Availability> availability
	) {
		final Map<DayOfWeek, TreeMap<LocalTime, Availability>> result =
			new HashMap<DayOfWeek, TreeMap<LocalTime, Availability>>();
		
		for (final Availability a : availability) {
			final DayOfWeek dayOfWeek =
				a.getDayOfTheWeek();
			
			final TreeMap<LocalTime, Availability> dayOfWeekAvailability =
				get(result, dayOfWeek);
			
			final LocalTime startTime =
				LocalTime.of(a.getStartHour(), a.getStartMinute());
			
			dayOfWeekAvailability.put(startTime, a);
		}
		
		return result;
	}

	private TreeMap<LocalTime, Availability> get(
		final Map<DayOfWeek, TreeMap<LocalTime, Availability>> result,
		final DayOfWeek dayOfWeek
	) {
		TreeMap<LocalTime, Availability> dayOfWeekAvailability =
			result.get(dayOfWeek);
		
		if (dayOfWeekAvailability == null) {
			dayOfWeekAvailability = new TreeMap<LocalTime, Availability>();
			result.put(dayOfWeek, dayOfWeekAvailability);
		}
		
		return dayOfWeekAvailability;
	}

	private <T extends DailyAgenda> TreeMap<LocalTime, Booking> next(
		final Iterator<Entry<Key<T>, T>> i
	) {
		final Entry<Key<T>, T> entry =
			i.next();
				
		final T value = 
			entry.getValue();
		
		if (value != null) {
			return value.bookingMap();
		}
		
		return null;
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
