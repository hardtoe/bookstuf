package com.bookstuf.web.booking;

import org.threeten.bp.LocalTime;

import com.bookstuf.datastore.Booking;
import com.bookstuf.datastore.PaymentMethod;
import com.bookstuf.datastore.PaymentStatus;

public class PublicBooking {
	public final boolean isPrivate;
	public boolean isRefundable;
	public final String title;
	public String id;
	public String professionalId;
	public String consumerId;
	public LocalTime startTime;
	
	public int top;
	public int height;

	public PublicBooking(final LocalTime startTime) {
		this.isPrivate = true;
		this.title = "";
		
		setStartTime(startTime);
	}

	public PublicBooking(final Booking visibleBooking) {
		this.isPrivate = false;
		
		this.isRefundable = 
			visibleBooking.getPaymentMethod() == PaymentMethod.STRIPE_CARD && 
			visibleBooking.getPaymentStatus() == PaymentStatus.PAID; 
		
		this.title = visibleBooking.getService().getName();
		this.id = visibleBooking.getId();
		this.professionalId = visibleBooking.getProfessional().getName();
		this.consumerId = visibleBooking.getConsumer().getName();
		
		final LocalTime consumerBookingStartTime = 
			visibleBooking.getStartTime();
		
		setStartTime(consumerBookingStartTime);
		setEndTime(consumerBookingStartTime.plus(visibleBooking.getService().getDuration()));
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