package com.bookstuf.web.booking;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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
import com.bookstuf.datastore.ProfessionalInformation;
import com.bookstuf.datastore.Service;
import com.bookstuf.web.Default;
import com.bookstuf.web.ExceptionHandler;
import com.bookstuf.web.Param;
import com.bookstuf.web.Publish;
import com.bookstuf.web.RequestBody;
import com.bookstuf.web.RpcServlet;
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.urlfetch.ResponseTooLargeException;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.identitytoolkit.GitkitClientException;
import com.google.identitytoolkit.GitkitUser;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;

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
	
	
	
	private Booking createBooking(
		final BookingRequest request
	) {
		// need to prepare as much information outside of transaction as 
		// possible to ensure transaction can execute as fast as possible
		final String consumerUserId =
			gitkitUser.get().getLocalId();
		
		final Key<ConsumerInformation> consumerKey = 
			Key.create(ConsumerInformation.class, consumerUserId);
		
		final Key<ProfessionalInformation> professionalKey = 
			Key.create(ProfessionalInformation.class, request.professionalUserId);


		final Booking requestedBooking =
			new Booking();
		
		// fill in booking information
		requestedBooking.setId(UUID.randomUUID().toString());
		requestedBooking.setProfessional(professionalKey);
		requestedBooking.setConsumer(consumerKey);
		requestedBooking.setStartTime(request.startTime);
		
		return requestedBooking;
	}
	

	
	// TODO: need to implement card deletion flow
	@Publish
	private String book(
		@RequestBody final BookingRequest request
	) throws 
		Exception 
	{	
		final Booking booking =
			createBooking(request);

		final PaymentStrategy paymentStrategy =
			getPaymentStrategy(request, booking);
		
		paymentStrategy.prepare();
		
		final BookingStrategy bookingStrategy =
			getBookingStrategy(request, booking);
		

		try {
			retryHelper.transactNew(10000, new Callable<Void>() {
				@Override
				public Void call() throws Exception {
						bookingStrategy.prepare();
						
						if (bookingStrategy.isBookingPossible()) {
							if (paymentStrategy.isPaymentLikely()) {
								final Service service =
									booking.getService();
								
								paymentStrategy.execute(service.getCost());
								
								bookingStrategy.execute();
								
								throw new IOException("TESTING EXCEPTION CASES!");
							} else {
								throw new RequestError("Unable to setup account for payment.");
							}
						} else {
							throw new RequestError("That time slot is not available.");
						}
					
					
					//return null;
				}
			});
		} catch (final Throwable throwable) {
			final Throwable t =
				getRealCause(throwable);
			
			if (t instanceof CardException) {
				throw new RequestError(t.getMessage());
				
			} else if (
				// payment may or may not have happened
				t instanceof IOException ||
				t instanceof SocketTimeoutException ||
				t instanceof ResponseTooLargeException ||
				
				// datastore transaction may or may not have happened
				t instanceof DatastoreTimeoutException ||
				t instanceof DatastoreFailureException ||
				
				// datastore transaction did not happen
				t instanceof ConcurrentModificationException
			) {	
				paymentStrategy.rollbackPayment();
				throw new RequestError("An internal error occured, try again later.");
				
			} else if (t instanceof RequestError) {
				throw (RequestError) t;
				
			} else {
				throw new RequestError("An internal error occured, try again later.");
			}
		}
		
		sendConfirmationToConsumer(request, booking);
		
		return "{\"success\": true}";
	}


	// TODO: need more information here and copy from Lena
	private void sendConfirmationToConsumer(
		final BookingRequest request, 
		final Booking booking
	) {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		final ConsumerInformation userInformation = 
			userService.getCurrentConsumerInformation().now();
		
		final String emailAddress =
			userInformation.getContactEmail();
		
		try {
		    final Message msg = 
		    	new MimeMessage(session);
		    
		    msg.setFrom(new InternetAddress("noreply@bookstuf.com", "Bookstuf Service"));
		    msg.addRecipient(Message.RecipientType.TO, new InternetAddress(emailAddress));
		    msg.setSubject("Booking confirmation for " + request.date);
		    msg.setText("Your booking has been successfully placed.");
		    
		    Transport.send(msg);

		} catch (final Exception e) {
		    logger.log(Level.WARNING, "unable to send booking confirmation", e);
		}
	}

	private BookingStrategy getBookingStrategy(
		final BookingRequest request,
		final Booking booking
	) {
		return new SingleBookingStrategy(request, booking);
	}

	private PaymentStrategy getPaymentStrategy(
		final BookingRequest request,
		final Booking booking
	) {
		if (request.paymentMethod == PaymentMethod.STRIPE_CARD) {
			return new StripePaymentStrategy(request, booking, userService, execService.get(), stripe, retryHelper);
			
		} else {
			return new CashPaymentStrategy(request, booking, userService, execService.get(), stripe, retryHelper);
		}
	}


	private List<Key<DailyAgenda>> getProfessionalDailyAgenda(
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
		
		return keys;
	}
	
	private List<Key<ConsumerDailyAgenda>> getConsumerDailyAgenda(
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
		
		return keys;
	}



	
	@Publish
	private List<PublicDailyAvailability> availability(
		@Param("professionalUserId") final String professionalUserId
	) {
		// TODO: change start date to the earliest date allowed for booking by professional
		final LocalDate startDate =
			LocalDate.now();
		
		final int NUM_DAYS = 7;
		
		// initiate fetch for daily agendas
		final List<Key<DailyAgenda>> professionalDailyAgendaKeys =
			getProfessionalDailyAgenda(professionalUserId, startDate, NUM_DAYS);

		final Map<Key<DailyAgenda>, DailyAgenda> professionalDailyAgendaResult =
			ofy().load().keys(professionalDailyAgendaKeys);
		
		
		final String consumerUserId =
			gitkitUser.get().getLocalId();

		final List<Key<ConsumerDailyAgenda>> consumerDailyAgendaKeys =
			getConsumerDailyAgenda(consumerUserId, startDate, NUM_DAYS);

		final Map<Key<ConsumerDailyAgenda>, ConsumerDailyAgenda> consumerDailyAgendaResult =
			ofy().load().keys(consumerDailyAgendaKeys);	

		
		final Key<ProfessionalInformation> professionalKey = 
			Key.create(ProfessionalInformation.class, professionalUserId);

		final LoadResult<ProfessionalInformation> professionalInfoResult =
			ofy().load().key(professionalKey);
		
		
		final ArrayList<PublicDailyAvailability> result =
			new ArrayList<>();

		final Iterator<Key<DailyAgenda>> professionalAgendaIterator =
			professionalDailyAgendaKeys.iterator();

		final Iterator<Key<ConsumerDailyAgenda>> consumerAgendaIterator =
			consumerDailyAgendaKeys.iterator();

		final LinkedList<Availability> availability =
			professionalInfoResult.safe().getAvailability();

		final Map<DayOfWeek, TreeMap<LocalTime, Availability>> dailyAvailability =
			sortAvailability(availability);
		
		int index = 0;
		
		while (
			consumerAgendaIterator.hasNext() &&
			professionalAgendaIterator.hasNext()
		) {
			final LocalDate date = 
				startDate.plusDays(index);
			
			final DayOfWeek dayOfWeek =
				date.getDayOfWeek();
	
			final TreeMap<LocalTime, Booking> consumerExistingBookings = 
				bookings(consumerDailyAgendaResult.get(consumerAgendaIterator.next()));
			
			logger.log(Level.INFO, "consumerExistingBookings[" + date + "] = " + consumerExistingBookings);
			
			final TreeMap<LocalTime, Booking> professionalExistingBookings = 
				bookings(professionalDailyAgendaResult.get(professionalAgendaIterator.next()));
			
			final PublicDailyAvailability publicDailyAvailability =
				new PublicDailyAvailability(
					index,
					date, 
					consumerExistingBookings, 
					professionalExistingBookings, 
					dailyAvailability.get(dayOfWeek));
			
			result.add(publicDailyAvailability);
			
			index++;
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

	private <T extends DailyAgenda> TreeMap<LocalTime, Booking> bookings(
		final T value
	) {
		if (value != null) {
			return value.bookingMap();
		}
		
		return null;
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
