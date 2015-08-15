package com.bookstuf.web.booking;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;

import com.bookstuf.datastore.PaymentMethod;

public class BookingRequest {
	public String professionalUserId;
	public String serviceId;
	public LocalDate date;
	public LocalTime startTime;
	public String timeZone;
	
	/**
	 * Cash or Credit
	 */
	public PaymentMethod paymentMethod;
	
	/**
	 * use this new card and add it to the customer
	 */
	public boolean addNewCard;
	
	/**
	 * set new credit card as default
	 */
	public boolean setNewCardAsDefault;
	
	/**
	 * new credit card information
	 */
	public String stripeToken;
	
	/**
	 * use this card instead of the default
	 */
	public String cardId;
}
