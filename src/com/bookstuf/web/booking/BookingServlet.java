package com.bookstuf.web.booking;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;

import static com.googlecode.objectify.ObjectifyService.ofy;

import com.bookstuf.Luke;
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
import com.bookstuf.datastore.Service;
import com.bookstuf.mail.Mail;
import com.bookstuf.web.AsTransaction;
import com.bookstuf.web.Default;
import com.bookstuf.web.ExceptionHandler;
import com.bookstuf.web.MustacheLoader;
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
import com.googlecode.objectify.Result;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Refund;
import com.stripe.model.Reversal;
import com.stripe.model.Transfer;

@Singleton
@SuppressWarnings("serial")
public class BookingServlet extends RpcServlet {
	private final Logger logger;
	private final UserManager userService;
	private final Provider<GitkitUser> gitkitUser;
	private final RetryHelper retryHelper;
	private final StripeApi stripe;
	private final Provider<ListeningExecutorService> execService;
	private MustacheLoader m;
	
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
	
	@Override
	public void init() throws ServletException {
		this.m = new MustacheLoader(getServletContext());
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
		
		final LoadResult<ConsumerInformation> consumerResult =
			ofy().load().key(Key.create(ConsumerInformation.class, gitkitUser.get().getLocalId()));
		
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

							} else {
								throw new RequestError("Unable to setup account for payment.");
							}
						} else {
							throw new RequestError("Someone reserved that slot just before you did.");
						}		
					
					return null;
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
				bookingStrategy.rollbackBooking();
				
				throw new RequestError("An internal error occured, try again later.");
				
			} else if (t instanceof RequestError) {
				throw (RequestError) t;
				
			} else {
				final ProfessionalInformation pro = 
					bookingStrategy.getProfessionalInformation();
				
				Luke.email(
					"booking exception", 
					
					"unknown internal error received while trying to complete a booking.\n" +
					"User: " + gitkitUser.get().getName() + " (" + gitkitUser.get().getEmail() + ")\n" +
					"Pro:  " + request.professionalUserId + ", " + (pro == null ? "null" : pro.getFirstName() + " " + pro.getLastName()), 
					
					t);
				
				throw new RequestError("An internal error occured, try again later.");
			}
		}

		final ProfessionalInformation pro = 
			bookingStrategy.getProfessionalInformation();
		
		
		try {
			sendBookingConfirmationEmail(request, booking, pro, consumerResult.now());
			
		} catch (final Exception e) {
			
			Luke.email(
				"booking email confirmation exception",
				
				"tried to send an email for a successful booking and an exception was thrown.\n" +					
				"User: " + gitkitUser.get().getName() + " (" + gitkitUser.get().getEmail() + ")\n" +
				"Pro:  " + request.professionalUserId + ", " + (pro == null ? "null" : pro.getFirstName() + " " + pro.getLastName()), 
						
				e);
			
			logger.log(Level.SEVERE, "tried to send an email for a successful booking and an exception was thrown.", e);
		}
		
		return "{\"success\": true}";
	}

	private void sendBookingConfirmationEmail(
		final BookingRequest request, 
		final Booking booking,
		final ProfessionalInformation professional,
		final ConsumerInformation consumer
	) {
		final ConsumerInformation userInformation = 
			userService.getCurrentConsumerInformation().now();
		
		final String emailAddress =
			userInformation.getContactEmail();
		
		try {
			Mail
				.from("noreply@bookstuf.com", "bookstuf.com")
				.to(emailAddress, consumer.getFirstName())
				.subject("Booking confirmation for " + request.date + " at " + request.startTime)
				.body(m.load("ConsumerBookingConfirmation.email"))
				.param("name", consumer.getFirstName())
				.param("date", request.date)
				.param("time", request.startTime)
				.param("service", booking.getService().getName())
				.param("cost", toDollarString(booking.getService().getCost()))
				.param("cash", request.paymentMethod == PaymentMethod.CASH)
				.param("card", request.paymentMethod == PaymentMethod.STRIPE_CARD)
				.send();
			
			Mail
				.from("noreply@bookstuf.com", "bookstuf.com")
				.to(professional.getContactEmail(), professional.getFirstName())
				.subject("Booking confirmation for " + request.date + " at " + request.startTime)
				.body(m.load("ProfessionalBookingConfirmation.email"))
				.param("name", professional.getFirstName())
				.param("clientName", consumer.getFirstName())  
				.param("date", request.date)
				.param("time", request.startTime)
				.param("service", booking.getService().getName())
				.param("cost", toDollarString(booking.getService().getCost()))
				.param("cash", request.paymentMethod == PaymentMethod.CASH)
				.param("card", request.paymentMethod == PaymentMethod.STRIPE_CARD)
				.send();

		} catch (final Exception e) {
		    logger.log(Level.WARNING, "unable to send booking confirmation", e);
		}
	}

	private String toDollarString(BigDecimal cost) {
		BigDecimal ONE_HUNDRED = new BigDecimal(100);
		return String.format("$%d.%02d", cost.intValue(), cost.multiply(ONE_HUNDRED).remainder(ONE_HUNDRED).intValue());
	}

	private BookingStrategy getBookingStrategy(
		final BookingRequest request,
		final Booking booking
	) {
		return new SingleBookingStrategy(request, booking, retryHelper);
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


	public static List<Key<DailyAgenda>> getProfessionalDailyAgenda(
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
				DailyAgenda.createProfessionalKey(professionalUserId, date);
			
			keys.add(key);
		}
		
		return keys;
	}
	
	public static List<Key<ConsumerDailyAgenda>> getConsumerDailyAgenda(
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
				ConsumerDailyAgenda.createConsumerKey(Key.create(ConsumerInformation.class, consumerUserId), date);
			
			keys.add(key);
		}
		
		return keys;
	}

	public static class BookingModificationRequest {
		public String professionalId;
		public String consumerId;
		public LocalDate date;
		public String bookingId;
		public String reason;
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("BookingModificationRequest [\n    professionalId=");
			builder.append(professionalId);
			builder.append(", \nconsumerId=");
			builder.append(consumerId);
			builder.append(", \ndate=");
			builder.append(date);
			builder.append(", \nbookingId=");
			builder.append(bookingId);
			builder.append(", \nreason=");
			builder.append(reason);
			builder.append("]");
			return builder.toString();
		}

	}
	
	private static class Reference<T> {
		public T value;
	}
	
	// the professional or the consumer can cancel
	@Publish
	private String cancel(
		@RequestBody final BookingModificationRequest request
	) {
		logger.info("received booking cancellation request: " + request);
		
		final String userId =
			gitkitUser.get().getLocalId();
		
		final Reference<Boolean> refunded =
			new Reference<>();
		
		final boolean requestedByPro = 
			userId.equals(request.professionalId);
		
		final boolean requestedByCon = 
			userId.equals(request.consumerId);
		
		logger.info("requestedByPro: " + requestedByPro);
		logger.info("requestedByCon: " + requestedByCon);
		
		if (requestedByPro || requestedByCon) {
			final String stripeRefundNonce = 
				UUID.randomUUID().toString();
			
			final Reference<ProfessionalInformation> professionalInformation =
				new Reference<ProfessionalInformation>();
			
			final Reference<Booking> booking =
				new Reference<Booking>();
			
			try {
				refunded.value = retryHelper.transactNew(10000, new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						boolean refunded = false;
						
						// fetch agendas with the booking
						final Result<DailyAgenda> professionalDailyAgendaResult =
							ofy().load().key(DailyAgenda.createProfessionalKey(request.professionalId, request.date));
						
						final LoadResult<ConsumerDailyAgenda> consumerDailyAgendaResult =
							ofy().load().key(ConsumerDailyAgenda.createConsumerKey(request.consumerId, request.date));
	
						final LoadResult<ProfessionalInformation> professionalInformationResult  =
							ofy().load().key(Key.create(ProfessionalInformation.class, request.professionalId));
	
						// wait for the daily agendas to be ready
						final DailyAgenda professionalDailyAgenda = 
							professionalDailyAgendaResult.now();
						
						booking.value =
							professionalDailyAgenda.getBooking(request.bookingId);
						
						logger.info("booking: " + booking);
		
						professionalInformation.value =
							professionalInformationResult.safe();
						
						if (
							(
								booking.value.getPaymentStatus() == PaymentStatus.PAID ||
								booking.value.getPaymentStatus() == null
							) &&
							booking.value.getPaymentMethod() == PaymentMethod.STRIPE_CARD
						) {
							logger.info("checking to see if it is eligible for refund");
							
							final int refundHours =
								professionalInformation.value.getCancellationDeadline();
							
							final LocalDateTime now =
								LocalDateTime.now();
							
							logger.info("current time: " + now);
							
							final LocalDateTime bookingTime =
								LocalDateTime.of(request.date, booking.value.getStartTime());
							
							logger.info("booking time: " + bookingTime);
							
							final LocalDateTime refundDeadline =
								bookingTime.minusHours(refundHours);

							logger.info("refund deadline: " + refundDeadline);
							
							if (now.isBefore(refundDeadline)) { 
								logger.info("before deadline, attempting a refund");
								
								final Charge charge =
									stripe
										.charge()
										.retrieve(booking.value.getStripeChargeId())
										.get();
								
								logger.info("got charge: " + charge);
								
								if (
									charge.getRefunded() && 
									(charge.getAmountRefunded().intValue() >= charge.getAmount().intValue())
								) {
									logger.info("already refunded, don't refund again");
									
								} else {
									logger.info("sending refund to stripe");
									
									final Refund refund =
										stripe
										.refund()
										.create(charge)
										.reverseTransfer(true)
										.refundApplicationFee(true)
										.metadata("reason", "refund due to cancellation " + (requestedByPro ? " by the professional" : " by the consumer	0..,"
												+ ""))
										.idempotencyKey(stripeRefundNonce)
										.get();
									
									logger.info("got refund: " + refund);
								}

								refunded = true;
							}	
						}
						
						final ConsumerDailyAgenda consumerDailyAgenda = 
							consumerDailyAgendaResult.now();
						
						if (refunded) {
							logger.info("setting refunded status on booking");
							
							professionalDailyAgenda.getBooking(booking.value.getId()).setPaymentStatus(PaymentStatus.REFUNDED);
							consumerDailyAgenda.getBooking(booking.value.getId()).setPaymentStatus(PaymentStatus.REFUNDED);
						}
						
						professionalDailyAgenda.cancelBooking(booking.value.getId());
						consumerDailyAgenda.cancelBooking(booking.value.getId());
						
						ofy().save().entities(professionalDailyAgenda);
						ofy().save().entities(consumerDailyAgenda);	
						
						return refunded;
					}
				});
			} catch (final Exception e) {
				throw new RequestError("Unable to complete request, try again later");
			}
			
			try {
				retryHelper.execute(5000, new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						final ConsumerInformation consumerInformation =
							ofy().load().key(Key.create(ConsumerInformation.class, request.consumerId)).now();
	
						Mail
							.from("noreply@bookstuf.com", "bookstuf.com")
							.to(consumerInformation.getContactEmail())
							.subject("Your booking on " + request.date + " at " + booking.value.getStartTime() + " has been cancelled" + (refunded.value ? " and refunded" : "") + (requestedByPro ? " by the professional" : ""))
							.body(m.load("ConsumerCancellation.email"))
							.param("name", consumerInformation.getFirstName())
							.param("isRefunded", refunded.value)
							.param("requestedByPro", requestedByPro)
							.param("requestedByCon", requestedByCon)
							.param("date", request.date)
							.param("time", booking.value.getStartTime())
							.param("service", booking.value.getService().getName())
							.param("cost", toDollarString(booking.value.getService().getCost()))
							.param("deadlineHours", professionalInformation.value.getCancellationDeadline())
							.send();
	
						Mail
							.from("noreply@bookstuf.com", "bookstuf.com")
							.to(professionalInformation.value.getContactEmail())
							.subject("Your booking on " + request.date + " at " + booking.value.getStartTime() + " has been cancelled" + (refunded.value ? " and refunded" : "") + (requestedByCon ? " by your client" : ""))
							.body(m.load("ProfessionalCancellation.email"))
							.param("name", professionalInformation.value.getFirstName())
							.param("isRefunded", refunded.value)
							.param("requestedByPro", requestedByPro)
							.param("requestedByCon", requestedByCon)
							.param("date", request.date)
							.param("time", booking.value.getStartTime())
							.param("service", booking.value.getService().getName())
							.param("cost", toDollarString(booking.value.getService().getCost()))
							.param("deadlineHours", professionalInformation.value.getCancellationDeadline())
							.send();
						
						return null;
					}
				});
				
			} catch (final Exception e) {
				Luke.email("unable to send cancellation email", "", e);
				logger.log(Level.SEVERE, "Unable to send cancellation email", e);
			}
				
			return "{\"success\": true}";			
		} else {
			throw new RequestError("Current user ID does not match professional ID of booking.");
		}
	}
	
	// only the professional can refund
	@Publish
	private String refund(
		@RequestBody final BookingModificationRequest request
	) throws 
		Exception 
	{
		final String stripeRefundNonce = 
			UUID.randomUUID().toString();
		
		final Reference<Booking> booking =
			new Reference<Booking>();
		
		final LoadResult<ConsumerInformation> consumerResult =
			ofy().load().key(Key.create(ConsumerInformation.class, request.consumerId));
		
		final LoadResult<ProfessionalInformation> professionalResult =
			ofy().load().key(Key.create(ProfessionalInformation.class, request.professionalId));
		
		if (gitkitUser.get().getLocalId().equals(request.professionalId)) {
			try {
				retryHelper.transactNew(10000, new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						// fetch agendas with the booking
						final Result<DailyAgenda> professionalDailyAgendaResult =
							ofy().load().key(DailyAgenda.createProfessionalKey(request.professionalId, request.date));
						
						final LoadResult<ConsumerDailyAgenda> consumerDailyAgendaResult =
							ofy().load().key(ConsumerDailyAgenda.createConsumerKey(request.consumerId, request.date));


						// wait for the daily agendas to be ready
						final DailyAgenda professionalDailyAgenda = 
							professionalDailyAgendaResult.now();
						
						booking.value =
							professionalDailyAgenda.getBooking(request.bookingId);

						if (
							booking.value.getPaymentStatus() == PaymentStatus.PAID &&
							booking.value.getPaymentMethod() == PaymentMethod.STRIPE_CARD
						) {
							final Charge charge =
								stripe
									.charge()
									.retrieve(booking.value.getStripeChargeId())
									.get();
							
							if (
								charge.getRefunded() && 
								(charge.getAmountRefunded().intValue() >= charge.getAmount().intValue())
							) {
								// already refunded, don't refund again
								
							} else {
								stripe
									.refund()
									.create(charge)
									.reverseTransfer(true)
									.refundApplicationFee(true)
									.metadata("reason", "professional issued refund")
									.idempotencyKey(stripeRefundNonce)
									.get();
							}
							
							booking.value.setPaymentStatus(PaymentStatus.REFUNDED);
							
							final ConsumerDailyAgenda consumerDailyAgenda = 
								consumerDailyAgendaResult.now();
							
							consumerDailyAgenda.removeBooking(booking.value.getId());
							consumerDailyAgenda.add(booking.value);
							
							ofy().save().entities(professionalDailyAgenda);
							ofy().save().entities(consumerDailyAgenda);	
							
							return true;
							
						} else {
							return false;
						}
					}
				});
			} catch (final Exception e) {
				throw new RequestError("Unable to complete request, try again later");
			}
			
			try {
				retryHelper.execute(5000, new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						final ProfessionalInformation professional =
							professionalResult.safe();
						
						final ConsumerInformation consumer =
							consumerResult.safe();
						
						Mail
							.from("noreply@bookstuf.com", "bookstuf.com")
							.to(consumer.getContactEmail(), consumer.getFirstName())
							.subject("Your booking on " + request.date + " at " + booking.value.getStartTime() + " has been refunded")
							.body(m.load("ConsumerRefund.email"))
							.param("name", consumer.getFirstName())
							.param("date", request.date)
							.param("time", booking.value.getStartTime())
							.param("service", booking.value.getService().getName())
							.param("cost", toDollarString(booking.value.getService().getCost()))
							.send();
						
						Mail
							.from("noreply@bookstuf.com", "bookstuf.com")
							.to(professional.getContactEmail(), professional.getFirstName())
							.subject("Your booking on " + request.date + " at " + booking.value.getStartTime() + " has been refunded")
							.body(m.load("ProfessionalRefund.email"))
							.param("name", professional.getFirstName())
							.param("date", request.date)
							.param("time", booking.value.getStartTime())
							.param("service", booking.value.getService().getName())
							.param("cost", toDollarString(booking.value.getService().getCost()))
							.send();
						
						return null;
					}
				});
				
			} catch (final Exception e) {
				Luke.email("unable to send refund email", "", e);
				logger.log(Level.SEVERE, "Unable to send refund email", e);
			}
				
			return "{\"success\": true}";
			
		} else {
			throw new RequestError("Current user ID does not match professional ID of booking.");
		}
	}

	@Publish 
	private List<PublicDailyAvailability> agenda() {
		// consumer agendas
		final String userId =
			gitkitUser.get().getLocalId();
		
		final LocalDate startDate =
			LocalDate.now();
		
		final int NUM_DAYS = 7;
		
		// initiate fetch for daily agendas and availiability
		final LoadResult<ProfessionalInformation> professionalInfoResult =
			ofy().load().key(Key.create(ProfessionalInformation.class, userId));
		
		final List<Key<DailyAgenda>> dailyAgendaKeysOne =
			getProfessionalDailyAgenda(userId, startDate, NUM_DAYS);

		final Map<Key<DailyAgenda>, DailyAgenda> dailyAgendaResultOne =
			ofy().load().keys(dailyAgendaKeysOne);

		final List<Key<ConsumerDailyAgenda>> dailyAgendaKeysTwo =
			getConsumerDailyAgenda(userId, startDate, NUM_DAYS);

		final Map<Key<ConsumerDailyAgenda>, ConsumerDailyAgenda> dailyAgendaResultTwo =
			ofy().load().keys(dailyAgendaKeysTwo);	
		

		final ArrayList<PublicDailyAvailability> result =
			new ArrayList<>();

		final Iterator<Key<DailyAgenda>> agendaIteratorOne =
			dailyAgendaKeysOne.iterator();

		final Iterator<Key<ConsumerDailyAgenda>> agendaIteratorTwo =
			dailyAgendaKeysTwo.iterator();

		final LinkedList<Availability> availability =
			professionalInfoResult.safe().getAvailability();

		final Map<DayOfWeek, TreeMap<LocalTime, Availability>> dailyAvailability =
			sortAvailability(availability);
		
		int index = 0;
		
		while (
			agendaIteratorOne.hasNext() &&
			agendaIteratorTwo.hasNext()
		) {
			final LocalDate date = 
				startDate.plusDays(index);
			
			final TreeMap<LocalTime, Booking> userBookings = 
				bookings(dailyAgendaResultOne.get(agendaIteratorOne.next()));
			
			if (
				userBookings != null && 
				dailyAgendaResultTwo != null
			) {
				final TreeMap<LocalTime, Booking> bookings = 
					bookings(dailyAgendaResultTwo.get(agendaIteratorTwo.next()));
				
				if (bookings != null) {
					userBookings.putAll(bookings);
				}
			}
			
			final PublicDailyAvailability publicDailyAvailability =
				new PublicDailyAvailability(
					index,
					date, 
					userBookings, 
					null,
					dailyAvailability.get(date.getDayOfWeek()));
			
			result.add(publicDailyAvailability);
			
			index++;
		}
		
		return result;
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
		final List<Key<DailyAgenda>> professionalDailyAgendaKeysOne =
			getProfessionalDailyAgenda(professionalUserId, startDate, NUM_DAYS);

		final Map<Key<DailyAgenda>, DailyAgenda> professionalDailyAgendaResultOne =
			ofy().load().keys(professionalDailyAgendaKeysOne);

		final List<Key<ConsumerDailyAgenda>> professionalDailyAgendaKeysTwo =
			getConsumerDailyAgenda(professionalUserId, startDate, NUM_DAYS);

		final Map<Key<ConsumerDailyAgenda>, ConsumerDailyAgenda> professionalDailyAgendaResultTwo =
			ofy().load().keys(professionalDailyAgendaKeysTwo);	
		
		
		// consumer agendas
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

		final Iterator<Key<DailyAgenda>> professionalAgendaIteratorOne =
			professionalDailyAgendaKeysOne.iterator();

		final Iterator<Key<ConsumerDailyAgenda>> professionalAgendaIteratorTwo =
			professionalDailyAgendaKeysTwo.iterator();

		final Iterator<Key<ConsumerDailyAgenda>> consumerAgendaIterator =
			consumerDailyAgendaKeys.iterator();

		final LinkedList<Availability> availability =
			professionalInfoResult.safe().getAvailability();

		final Map<DayOfWeek, TreeMap<LocalTime, Availability>> dailyAvailability =
			sortAvailability(availability);
		
		int index = 0;
		
		while (
			consumerAgendaIterator.hasNext() &&
			professionalAgendaIteratorOne.hasNext() &&
			professionalAgendaIteratorTwo.hasNext()
		) {
			final LocalDate date = 
				startDate.plusDays(index);
			
			final DayOfWeek dayOfWeek =
				date.getDayOfWeek();
	
			final TreeMap<LocalTime, Booking> consumerExistingBookings = 
				bookings(consumerDailyAgendaResult.get(consumerAgendaIterator.next()));
			
			logger.log(Level.INFO, "consumerExistingBookings[" + date + "] = " + consumerExistingBookings);
			
			final TreeMap<LocalTime, Booking> professionalExistingBookings = 
				bookings(professionalDailyAgendaResultOne.get(professionalAgendaIteratorOne.next()));
			
			if (
				professionalExistingBookings != null && 
				professionalDailyAgendaResultTwo != null
			) {
				final TreeMap<LocalTime, Booking> bookings = 
					bookings(professionalDailyAgendaResultTwo.get(professionalAgendaIteratorTwo.next()));
				
				if (bookings != null) {
					professionalExistingBookings.putAll(bookings);
				}
			}
			
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
	
	public static Map<DayOfWeek, TreeMap<LocalTime, Availability>> sortAvailability(
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

	public static TreeMap<LocalTime, Availability> get(
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

	public static <T extends DailyAgenda> TreeMap<LocalTime, Booking> bookings(
		final T value
	) {
		if (value != null) {
			return value.bookingMap();
		}
		
		return null;
	}
}
