package com.bookstuf.web.booking;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.TextStyle;

import com.bookstuf.datastore.Availability;
import com.bookstuf.datastore.Booking;

public class PublicDailyAvailability {
	public final int index;
	public final String dayOfWeek;
	public final String month;
	public final int monthNumber;
	public final int dayOfMonth;
	public final int year;
	public final List<PublicBooking> bookedTimes;
	
	public PublicDailyAvailability(
		final int index,
		final LocalDate day,
		final TreeMap<LocalTime, Booking> consumerAgenda,
		final TreeMap<LocalTime, Booking> professionalAgenda,
		final TreeMap<LocalTime, Availability> professionalAvailability
	) {
		this.index = index;
		this.dayOfWeek = day.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);
		this.month = day.getMonth().getDisplayName(TextStyle.FULL, Locale.US);
		this.monthNumber = day.getMonthValue();
		this.dayOfMonth = day.getDayOfMonth();
		this.year = day.getYear();
		this.bookedTimes = new ArrayList<>();
		
		// iterate over the entire day in 15 minute increments
		PublicBooking currentBookedRange = null;
		
		for (int minutes = 0; minutes < (24 * 60); minutes += 15) {
			final LocalTime t = 
				LocalTime.ofSecondOfDay(minutes * 60);

			final boolean previousSlotIsAvailable =
				currentBookedRange == null;
			
			final boolean previousSlotIsBooked =
				!previousSlotIsAvailable;
			
			final boolean currentSlotIsAvailable =
				isAvailable(t, professionalAgenda, professionalAvailability);
			
			final boolean currentSlotIsBooked =
				!currentSlotIsAvailable;

			// this is the end of a booked range, need to close it out
			if (previousSlotIsBooked && currentSlotIsAvailable) {
				currentBookedRange.setEndTime(t);
				bookedTimes.add(currentBookedRange);
				currentBookedRange = null;
			}

			// this is the beginning of a booked range, need to create it
			if (previousSlotIsAvailable && currentSlotIsBooked) {
				currentBookedRange = new PublicBooking(t);
			}
			
			// consumer has an appointment at this time, add it directly
			// so they can see what appointment it is
			final Booking consumerBooking =
				get(consumerAgenda, t);
			
			if (consumerBooking != null) {
				bookedTimes.add(new PublicBooking(consumerBooking));
			}
		}
		
		// need to close out the final booked range of the day
		if (currentBookedRange != null) {
			currentBookedRange.setEndTime(LocalTime.MAX);
			bookedTimes.add(currentBookedRange);
		}
		
	}
	
	private Booking get(
		final TreeMap<LocalTime, Booking> consumerAgenda,
		final LocalTime t
	) {
		if (consumerAgenda != null) {
			return consumerAgenda.get(t);
		} 
		
		return null;
	}

	private <T> T floorValue(
		final TreeMap<LocalTime, T> map, 
		final LocalTime t
	) {
		if (map != null) {
			final Entry<LocalTime, T> entry =
				map.floorEntry(t);
			
			if (entry != null) {
				return entry.getValue();
				
			}
		}
		
		return null;
	}
	
	private boolean isAvailable(
		final LocalTime slotStart,
		final TreeMap<LocalTime, Booking> bookingMap,
		final TreeMap<LocalTime, Availability> sortedAvailability
	) {
		final Booking booking =
			floorValue(bookingMap, slotStart);
		
		final Availability availability =
			floorValue(sortedAvailability, slotStart);
		
		return
			overlaps(slotStart, availability) &&
			!overlaps(slotStart, booking);
	}

	private boolean overlaps(
		final LocalTime slotStart,
		final Booking booking
	) {
		if (booking == null) {
			return false;
		}
		
		final LocalTime bookingStart = 
			booking.getStartTime();
		
		final LocalTime bookingEnd = 
			bookingStart.plus(booking.getService().getDuration());
		
		return
			bookingEnd.isAfter(slotStart);
	}

	private boolean overlaps(
		final LocalTime slotStart,
		final Availability availability
	) {
		if (availability == null) {
			return false;
		}
		
		final LocalTime availEnd =
			LocalTime.of(availability.getEndHour(), availability.getEndMinute());
		
		return
			availEnd.isAfter(slotStart);		
	}
}