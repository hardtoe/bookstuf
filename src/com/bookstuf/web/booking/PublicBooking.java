package com.bookstuf.web.booking;

import org.threeten.bp.LocalTime;

import com.bookstuf.datastore.Booking;

public class PublicBooking {
	public final boolean isPrivate;
	public final String title;
	public LocalTime startTime;
	
	public int top;
	public int height;

	public PublicBooking(final LocalTime startTime) {
		this.isPrivate = true;
		this.title = "";
		
		setStartTime(startTime);
	}

	public PublicBooking(final Booking consumerBooking) {
		this.isPrivate = false;
		this.title = consumerBooking.getService().getName();
		
		setStartTime(consumerBooking.getStartTime());
		setEndTime(consumerBooking.getStartTime().plus(consumerBooking.getService().getDuration()));
	}

	private void setStartTime(final LocalTime time) {
		this.startTime = time;
		this.top = getPixels(time);
	}
	
	private int getPixels(final LocalTime time) {
		return (time.getHour() * 50) + ((time.getMinute() * 50) / 60);
	}

	public void setEndTime(final LocalTime endTime) {
		this.height = getPixels(endTime) - top;
	}
}