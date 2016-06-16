package com.bookstuf.web;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.bookstuf.appengine.UserManager;
import com.bookstuf.datastore.Booking;
import com.bookstuf.datastore.ConsumerDailyAgenda;
import com.bookstuf.datastore.ConsumerInformation;
import com.bookstuf.datastore.DailyAgenda;
import com.bookstuf.ical.AlarmAction;
import com.bookstuf.ical.EventStatus;
import com.bookstuf.ical.ICal;
import com.bookstuf.ical.ICalBuilder;
import com.bookstuf.web.booking.BookingServlet;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
 
@Singleton
public class ICalServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final Provider<UserManager> userService;
	private final Logger logger;

	@Inject ICalServlet(
		final Provider<UserManager> userService,
		final Logger logger
	) {
		this.userService = userService;
		this.logger = logger;
	}
	
	@Override
	public void doGet(
		final HttpServletRequest req, 
		final HttpServletResponse rsp
	) throws 
		IOException 
	{
		// FIXME: this needs to be changed to a random key for security
		final String userId =
			req.getServletPath().split("/")[1];
		
		// FIXME: when a pro is created it should also create ConsumerInformation
		final LoadResult<ConsumerInformation> informationResult =
			ofy().load().key(Key.create(ConsumerInformation.class, userId));
		
		final LocalDate startDate =
			LocalDate.now();
		
		final int NUM_DAYS = 28;
		
		// initiate fetch for daily agendas and availiability
		final List<Key<DailyAgenda>> dailyAgendaKeysOne =
			BookingServlet.getProfessionalDailyAgenda(userId, startDate, NUM_DAYS);

		final Map<Key<DailyAgenda>, DailyAgenda> dailyAgendaResultOne =
			ofy().load().keys(dailyAgendaKeysOne);

		final List<Key<ConsumerDailyAgenda>> dailyAgendaKeysTwo =
			BookingServlet.getConsumerDailyAgenda(userId, startDate, NUM_DAYS);

		final Map<Key<ConsumerDailyAgenda>, ConsumerDailyAgenda> dailyAgendaResultTwo =
			ofy().load().keys(dailyAgendaKeysTwo);	
		
		final ConsumerInformation info =
			informationResult.safe();

		final Iterator<Key<DailyAgenda>> agendaIteratorOne =
			dailyAgendaKeysOne.iterator();

		final Iterator<Key<ConsumerDailyAgenda>> agendaIteratorTwo =
			dailyAgendaKeysTwo.iterator();
		
		int index = 0;
		
		final ICalBuilder ical = ICal
			.beginCalendar()
				.prodid("Luke Valenty", "bookstuf.com 1.0")
				.version("2.0")
				.calscale("GREGORIAN")
				.method("PUBLISH")
				.calname("bookstuf.com");

		while (
			agendaIteratorOne.hasNext() &&
			agendaIteratorTwo.hasNext()
		) {
			final LocalDate date = 
				startDate.plusDays(index);
			
			final DailyAgenda agendaOne = 
				dailyAgendaResultOne.get(agendaIteratorOne.next());
			
			if (agendaOne != null) {
				addEvents(ical, date, agendaOne.getTimezone(), agendaOne.bookingMap());
			}
			
			final ConsumerDailyAgenda agendaTwo = 
				dailyAgendaResultTwo.get(agendaIteratorTwo.next());
			
			if (agendaTwo != null) {
				addEvents(ical, date, agendaTwo.getTimezone(), agendaTwo.bookingMap());
			}
			
			index++;
		}

		ical.endCalendar().output(rsp.getWriter());
	}

	private void addEvents(
		final ICalBuilder ical, 
		final LocalDate date,
		final ZoneId timezone,
		final TreeMap<LocalTime, Booking> bookings
	) {
		if (bookings != null) {
			for (final Booking b : bookings.values()) {
				addEvent(ical, date, timezone, b);
			}
		}
	}

	private void addEvent(
		final ICalBuilder ical, 
		final LocalDate date, 
		final ZoneId timezone,
		final Booking b
	) {
		final ZonedDateTime startTime =
			ZonedDateTime.of(date, b.getStartTime(), timezone);
		
		final ZonedDateTime endTime =
			startTime.plus(b.getService().getDuration());
		
		ical
			.beginEvent()
				.uid(b.getId())
				.dtstart(startTime)
				.dtend(endTime)
				.summary(b.getService().getName())
				.description(b.getService().getDescription())
				
				.location(b.getLocation())
				
				.status(EventStatus.CONFIRMED)
				.attendee(b.getProfessionalName(), b.getProfessionalEmail())
				.attendee(b.getConsumerName(), b.getConsumerEmail())
				
				.beginAlarm()
					.action(AlarmAction.DISPLAY)
					.trigger(startTime.minusDays(1))
					.description("appointment in 1 day")
				.endAlarm()
				
				.beginAlarm()
					.action(AlarmAction.DISPLAY)
					.trigger(startTime.minusHours(1))
					.description("appointment in 1 hour")
				.endAlarm()
				
			.endEvent();
	}
}
