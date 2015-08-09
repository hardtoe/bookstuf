package com.bookstuf.web.card;

public class ExpiredCardException extends Exception {
	public final int expYear;
	public final int expMonth;
	
	public ExpiredCardException(
		final int expYear, 
		final int expMonth
	) {
		this.expYear = expYear;
		this.expMonth = expMonth;
	}
}