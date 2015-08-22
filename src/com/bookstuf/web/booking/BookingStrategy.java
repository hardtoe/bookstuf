package com.bookstuf.web.booking;

import com.bookstuf.datastore.ProfessionalInformation;

public abstract class BookingStrategy {

	public abstract void prepare();
	public abstract boolean isBookingPossible();
	public abstract void execute();
	public abstract void rollbackBooking();
	
	public abstract ProfessionalInformation getProfessionalInformation();
}
