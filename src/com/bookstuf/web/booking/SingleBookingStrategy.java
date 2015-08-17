package com.bookstuf.web.booking;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.LinkedList;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;

import com.bookstuf.datastore.Availability;
import com.bookstuf.datastore.Booking;
import com.bookstuf.datastore.ConsumerDailyAgenda;
import com.bookstuf.datastore.ConsumerInformation;
import com.bookstuf.datastore.DailyAgenda;
import com.bookstuf.datastore.ProfessionalInformation;
import com.bookstuf.datastore.Service;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Result;

public class SingleBookingStrategy extends BookingStrategy {
	private final BookingRequest request;
	private final Booking booking;
	private DailyAgenda professionalDailyAgenda;
	private ConsumerDailyAgenda consumerDailyAgenda;
	private ProfessionalInformation professionalInfo;
	private LinkedList<Availability> availability;
	
	public SingleBookingStrategy(
		final BookingRequest request,
		final Booking booking
	) {
		this.request = request;
		this.booking = booking;
	}
	
	@Override
	public void prepare() {
		// initiate fetch for daily agendas
		final Result<DailyAgenda> professionalDailyAgendaResult =
			getProfessionalDailyAgenda(request.professionalUserId, request.date);
		
		final Result<ConsumerDailyAgenda> consumerDailyAgendaResult =
			getConsumerDailyAgenda(booking.getConsumer(), request.date);

		final LoadResult<ProfessionalInformation> professionalInfoResult =
			ofy().load().key(booking.getProfessional());


		// wait for the daily agendas to be ready
		professionalDailyAgenda =
			professionalDailyAgendaResult.now();
		
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
			professionalDailyAgenda.canAdd(booking) &&
			consumerDailyAgenda.canAdd(booking);
	}

	@Override
	public void execute() {
		// ...make it if we can...
		professionalDailyAgenda.add(booking);
		consumerDailyAgenda.add(booking);
		
		// save daily agendas back to the datastore
		ofy().save().entity(professionalDailyAgenda);
		ofy().save().entity(consumerDailyAgenda);
	}

	private Result<DailyAgenda> getProfessionalDailyAgenda(
		final String professionalUserId, 
		final LocalDate date
	) {
		final Key<DailyAgenda> key =
			DailyAgenda.createKey(professionalUserId, date);
			
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
			ConsumerDailyAgenda.createKey(consumerKey, consumerUserId, date);
		
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
}
