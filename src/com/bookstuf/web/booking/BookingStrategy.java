package com.bookstuf.web.booking;

public abstract class BookingStrategy {

	public abstract void prepare();
	public abstract boolean isBookingPossible();
	public abstract void execute();
}
