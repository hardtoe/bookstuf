package com.bookstuf.datastore;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;

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
	
	@Index int numBookings;
	@Index boolean hasBookings;
	
	public DailyAgenda() {
		this.bookings = new TreeMap<LocalTime, Booking>();
	}
	
	public void setOwnerAndDate(
		final String gitkitUserId,
		final LocalDate date
	) {
		ownerAndDate =
			createKeyString(gitkitUserId, date);
	}
	
	protected static String createKeyString(
		final String gitkitUserId,
		final LocalDate date
	) {
		return gitkitUserId + ":" + date.toString();
	}
	
	public static Key<DailyAgenda> createKey(
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
		// special case, booking is exactly the same, return true
		// this behavior is needed to make booking transactions 
		// idempotent in the datastore and safe to retry
		final Booking sameBooking =
			bookings.get(newBooking.getStartTime());
		
		if (
			sameBooking != null && 
			sameBooking.equals(newBooking)
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
	}

	public TreeMap<LocalTime, Booking> bookingMap() {
		return bookings;
	}
}
