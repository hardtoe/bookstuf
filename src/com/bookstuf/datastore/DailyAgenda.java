package com.bookstuf.datastore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.OnSave;
import com.googlecode.objectify.annotation.Serialize;

@Cache @Entity
public class DailyAgenda {
	@Id String ownerAndDate;
	
	@Serialize TreeMap<LocalTime, Booking> bookings;
	@Serialize ArrayList<Booking> cancelledBookings;
	
	@Index int numBookings;
	@Index boolean hasBookings;
	
	@Index Date date;
	
	public DailyAgenda() {
		this.bookings = new TreeMap<LocalTime, Booking>();
		this.cancelledBookings = new ArrayList<Booking>();
	}
	
	public void setOwnerAndDate(
		final String gitkitUserId,
		final LocalDate date
	) {
		this.ownerAndDate =
			createKeyString(gitkitUserId, date);
		
		this.date =
			new Date(date.atTime(0, 0).toEpochSecond(ZoneOffset.UTC) * 1000L);
	}
	
	private static Date fromLocalDate(final LocalDate localDate) {
		return new Date(localDate.atTime(0, 0).toEpochSecond(ZoneOffset.UTC) * 1000L);
	}
	
	protected static String createKeyString(
		final String gitkitUserId,
		final LocalDate date
	) {
		return gitkitUserId + ":" + date.toString();
	}
	
	public static Key<DailyAgenda> createProfessionalKey(
		final String gitkitUserId,
		final LocalDate date
	) {
		return Key.create(DailyAgenda.class, createKeyString(gitkitUserId, date));
	}
	
	public String getOwner() {
		return ownerAndDate.split(":")[0];
	}
	
	public LocalDate getDate() {
		return LocalDate.parse(ownerAndDate.split(":")[1]);
	}
	
	public Collection<Booking> allBookings() {
		return bookings.values();
	}
	
	public void add(final Booking newBooking) {
		bookings.put(newBooking.getStartTime(), newBooking);
	}
	
	public boolean canAdd(final Booking newBooking) {		
		// special case, booking is the same id, need to
		// return so we can preserve idempotent semantics
		// for booking transaction
		final Booking sameBooking =
			bookings.get(newBooking.getStartTime());
		
		if (
			sameBooking != null && 
			sameBooking.getId().equals(newBooking.getId())
		) {
			return true;
		}
		
		// check for earlier bookings that overlap
		final Entry<LocalTime, Booking> earlierBooking = 
			bookings.floorEntry(newBooking.getStartTime());
		
		if (
			earlierBooking != null &&
			overlapsWith(earlierBooking.getValue(), newBooking)
		) {
			return false;
		}
		
		// check for later bookings that overlap
		final Entry<LocalTime, Booking> laterBooking =
			bookings.ceilingEntry(newBooking.getStartTime());
		
		if (
			laterBooking != null &&
			overlapsWith(newBooking, laterBooking.getValue())
		) {
			return false;
		}

		// no conflicts!
		return true;
	}

	private boolean overlapsWith(
		final Booking lhs,
		final Booking rhs
	) {
		final LocalTime lhsEndTime =
			lhs.getStartTime().plus(lhs.getService().getDuration());
		
		return
			lhsEndTime.isAfter(rhs.getStartTime());
	}
	
	@OnSave public void onSave() {
		numBookings = bookings.size();
		hasBookings = numBookings > 0;
		
		if (date == null) {
			date = fromLocalDate(getDate());
		}
	}

	public TreeMap<LocalTime, Booking> bookingMap() {
		return bookings;
	}

	public boolean removeBooking(final String id) {
		final Iterator<Entry<LocalTime, Booking>> i = 
			bookings.entrySet().iterator();
		
		while (i.hasNext()) {
			final Booking booking =
				i.next().getValue();
			
			if (booking.getId().equals(id)) {
				i.remove();
				return true;
			}
		}
		
		return false;
	}

	public Booking getBooking(final String id) {
		final Iterator<Entry<LocalTime, Booking>> i = 
			bookings.entrySet().iterator();
		
		while (i.hasNext()) {
			final Booking booking =
				i.next().getValue();
			
			if (booking.getId().equals(id)) {
				return booking;
			}
		}
		
		return null;
	}

	public void cancelBooking(String id) {
		final Booking booking =
			getBooking(id);
		
		removeBooking(id);
		
		cancelledBookings.add(booking);
	}
}
