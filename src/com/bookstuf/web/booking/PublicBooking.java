package com.bookstuf.web.booking;

import org.threeten.bp.LocalTime;

import com.bookstuf.datastore.Booking;

public class PublicBooking {
	public final boolean isPrivate;
	public final String title;
	
	public final LocalTime startTime;
	public LocalTime endTime;

	public PublicBooking(final LocalTime startTime) {
		this.isPrivate = true;
		this.title = "";
		this.startTime = startTime;
	}

	public PublicBooking(final Booking consumerBooking) {
		this.isPrivate = false;
		this.title = consumerBooking.getService().getName();
		this.startTime = consumerBooking.getStartTime();
		this.endTime = consumerBooking.getStartTime().plus(consumerBooking.getService().getDuration());
	}

	public void setEndTime(final LocalTime endTime) {
		this.endTime = endTime;
	}
}