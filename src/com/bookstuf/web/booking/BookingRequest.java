package com.bookstuf.web.booking;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;

import com.bookstuf.datastore.PaymentMethod;
import com.bookstuf.datastore.Service;

public class BookingRequest {
	public String professionalUserId;
	public Service service;
	public LocalDate date;
	public LocalTime startTime;
	
	/**
	 * Cash or Credit
	 */
	public PaymentMethod paymentMethod;
	
	/**
	 * create new stripe customer if this is set
	 */
	public boolean isNewStripeCustomer;
	
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