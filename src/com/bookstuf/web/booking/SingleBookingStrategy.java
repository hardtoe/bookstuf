package com.bookstuf.web.booking;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;

import com.bookstuf.Luke;
import com.bookstuf.appengine.RetryHelper;
import com.bookstuf.datastore.Availability;
import com.bookstuf.datastore.Booking;
import com.bookstuf.datastore.ConsumerDailyAgenda;
import com.bookstuf.datastore.ConsumerInformation;
import com.bookstuf.datastore.DailyAgenda;
import com.bookstuf.datastore.ProfessionalInformation;
import com.bookstuf.datastore.Service;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Result;

public class SingleBookingStrategy extends BookingStrategy {
	private static final Logger log = 
		Logger.getLogger(SingleBookingStrategy.class.getCanonicalName());
	
	private final BookingRequest request;
	private final Booking booking;
	private final RetryHelper retryHelper;
	
	private DailyAgenda professionalDailyAgendaOne;
	private DailyAgenda professionalDailyAgendaTwo;
	private ConsumerDailyAgenda consumerDailyAgenda;
	private ProfessionalInformation professionalInfo;
	private LinkedList<Availability> availability;
	
	public SingleBookingStrategy(
		final BookingRequest request,
		final Booking booking,
		final RetryHelper retryHelper
	) {
		this.request = request;
		this.booking = booking;
		this.retryHelper = retryHelper;
	}
	
	@Override
	public void prepare() {
		// initiate fetch for daily agendas
		final Result<DailyAgenda> professionalDailyAgendaResultOne =
			getProfessionalDailyAgenda(request.professionalUserId, request.date);
		
		final Result<ConsumerDailyAgenda> professionalDailyAgendaResultTwo =
			getConsumerDailyAgenda(Key.create(ConsumerInformation.class, request.professionalUserId), request.date);
		
		final Result<ConsumerDailyAgenda> consumerDailyAgendaResult =
			getConsumerDailyAgenda(booking.getConsumer(), request.date);

		final LoadResult<ProfessionalInformation> professionalInfoResult =
			ofy().load().key(booking.getProfessional());


		// wait for the daily agendas to be ready
		professionalDailyAgendaOne =
			professionalDailyAgendaResultOne.now();
		
		professionalDailyAgendaTwo =
			professionalDailyAgendaResultTwo.now();
		
		consumerDailyAgenda =
			consumerDailyAgendaResult.now();
		
		professionalInfo = 
			professionalInfoResult.safe();
		
		availability =
			professionalInfo.getAvailability();

		final Service service =
			professionalInfo.getService(request.serviceId);
		
		booking.setService(service);
	}

	@Override
	public boolean isBookingPossible() {
		return 						
			isAvailable(request.date.getDayOfWeek(), availability, booking) &&
			professionalDailyAgendaOne.canAdd(booking) &&
			professionalDailyAgendaTwo.canAdd(booking) &&
			consumerDailyAgenda.canAdd(booking);
	}

	@Override
	public void execute() {
		// ...make it if we can...
		professionalDailyAgendaOne.add(booking);
		consumerDailyAgenda.add(booking);
		
		// save daily agendas back to the datastore
		ofy().save().entity(professionalDailyAgendaOne);
		ofy().save().entity(consumerDailyAgenda);
	}

	private Result<DailyAgenda> getProfessionalDailyAgenda(
		final String professionalUserId, 
		final LocalDate date
	) {
		final Key<DailyAgenda> key =
			DailyAgenda.createProfessionalKey(professionalUserId, date);
			
		final LoadResult<DailyAgenda> result =
			ofy().load().key(key);
		
		return new Result<DailyAgenda>() {
			@Override
			public DailyAgenda now() {
				try {
					return result.safe();
					
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
		final LocalDate date
	) {
		final String consumerUserId =
			consumerKey.getName();
		
		final Key<ConsumerDailyAgenda> key =
			ConsumerDailyAgenda.createConsumerKey(consumerKey, date);
		
		final LoadResult<ConsumerDailyAgenda> result =
			ofy().load().key(key);
		
		return new Result<ConsumerDailyAgenda>() {
			@Override
			public ConsumerDailyAgenda now() {
				try {
					return result.safe();
					
				} catch (final NotFoundException e) {
					final ConsumerDailyAgenda dailyAgenda =
						new ConsumerDailyAgenda();
					
					dailyAgenda.setOwnerAndDate(consumerUserId, date);
					
					return dailyAgenda;
				}
			}	
		};
	}

	private boolean isAvailable(
		final DayOfWeek dayOfWeek,
		final LinkedList<Availability> availability,
		final Booking requestedBooking
	) {
		for (final Availability a : availability) {
			if (a.getDayOfTheWeek() == dayOfWeek) {
				
				final LocalTime availStartTime =
					LocalTime.of(a.getStartHour(), a.getStartMinute());
				
				final LocalTime availEndTime =
					LocalTime.of(a.getEndHour(), a.getEndMinute());
				
				final LocalTime bookingStartTime = 
					requestedBooking.getStartTime();
				
				final LocalTime bookingEndTime =
					bookingStartTime.plus(requestedBooking.getService().getDuration());
				
				if (
					(
						bookingStartTime.equals(availStartTime) ||
						bookingStartTime.isAfter(availStartTime)
					) && (
						bookingEndTime.equals(availEndTime) ||
						bookingEndTime.isBefore(availEndTime)
					)
				) {
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public void rollbackBooking() {
		log.log(Level.WARNING, "SingleBookingSystem.rollbackBooking() - enter");
		log.log(Level.WARNING, "date = " + request.date);
		log.log(Level.WARNING, "time = " + request.startTime);
		log.log(Level.WARNING, "booking.id = " + booking.getId());
		
		try {
			retryHelper.execute(10000, new Callable<TaskHandle>() {
				@Override
				public TaskHandle call() throws Exception {
					log.log(Level.INFO, "attempting to enqueue BookingCleanupTask");
					
					final Queue q = 
						QueueFactory.getQueue("booking-cleanup");
					
					return q.add(TaskOptions.Builder
						.withPayload(new BookingCleanupTask(request.date, booking))
						.taskName("BookingCleanupTask-" + booking.getId()));
				}});
			
		} catch (final Throwable t) {
			log.log(Level.SEVERE, "FATAL - ENQUEUE: failed to enqueue cleanup task! this booking id " + booking.getId() + " needs to be manually removed!", t);
			
			sendAdminEmail(t);
		}
	}
	
	@Override
	public ProfessionalInformation getProfessionalInformation() {
		return this.professionalInfo;
	}
	
	private void sendAdminEmail(final Throwable t) {
		Luke.email(
			"booking cleanup failure for booking id " + booking.getId(), 
			
			"Could not enqueue booking cleanup for booking id " + booking.getId() + ".  \n" +
			"This needs to be manually cleaned up.",
		    		
			t);
	}
}
