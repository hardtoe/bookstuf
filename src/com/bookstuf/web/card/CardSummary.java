package com.bookstuf.web.card;

public class CardSummary {
	public String id;
	public boolean isDefault;
	public String brand;
	public String last4;
	public int expMonth;
	public int expYear;
	public boolean deleted;
	
	public CardSummary(
		final String id,
		final boolean isDefault,
		final String brand,
		final String last4,
		final int expMonth,
		final int expYear, 
		final boolean deleted
	) {
		this.id = id;
		this.isDefault = isDefault;
		this.brand = brand;
		this.last4 = last4;
		this.expMonth = expMonth;
		this.expYear = expYear;
		this.deleted = deleted;
	}
}