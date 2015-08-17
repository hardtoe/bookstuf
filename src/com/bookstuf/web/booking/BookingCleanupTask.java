package com.bookstuf.web.booking;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.threeten.bp.LocalDate;

import com.bookstuf.datastore.Booking;
import com.bookstuf.datastore.ConsumerDailyAgenda;
import com.bookstuf.datastore.DailyAgenda;
import com.bookstuf.datastore.ProfessionalInformation;
import com.google.appengine.api.taskqueue.DeferredTask;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Result;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.util.Closeable;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class BookingCleanupTask implements DeferredTask {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(BookingCleanupTask.class.getCanonicalName());
	
	private Booking booking;
	private LocalDate date;

	public BookingCleanupTask() {
		// needed for serialization?
	}
	
	public BookingCleanupTask(
		final LocalDate date, 
		final Booking booking
	) {
		this.date = date;
		this.booking = booking;
	}

	@Override
	public void run() {
		log.log(Level.INFO, "starting BookingCleanupTask for booking id " + booking.getId());
		
		Closeable closeable = ObjectifyService.begin();
		
		ofy().transact(new Work<Void>() {
			@Override
			public Void run() {
				final Key<DailyAgenda> professionalAgendaKey =
					DailyAgenda.createProfessionalKey(booking.getProfessional().getName(), date);
				
				final Key<ConsumerDailyAgenda> consumerAgendaKey =
					ConsumerDailyAgenda.createConsumerKey(booking.getConsumer(), date);

				DailyAgenda professionalAgenda =
					ofy().load().key(professionalAgendaKey).now();

				if (
					professionalAgenda != null &&
					professionalAgenda.removeBooking(booking.getId())
				) {
					log.log(Level.INFO, "booking was found and removed from professional agenda");
				}
				
				ConsumerDailyAgenda consumerAgenda =
					ofy().load().key(consumerAgendaKey).now();

				if (
					consumerAgenda != null &&
					consumerAgenda.removeBooking(booking.getId())
				) {
					log.log(Level.INFO, "booking was found and removed from consumer agenda");
				}
					
				return null;
			}});
		
		closeable.close();
	}

}
