package com.bookstuf.datastore;

import java.util.Date;

import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Cache @Entity
public class Bill {
	@Id String bookingId;

	PaymentMethod paymentMethod;
	String stripeCustomerId;
	String stripeCardId;
	
	@Index Date nextChargeAttempt;
	@Index boolean isActive;
	PaymentStatus paymentStatus;
	String nonce;
	
	int numCardRetries;
	int numOtherRetries;
	
	public ZonedDateTime getNextChargeAttempt() {
		return ZonedDateTime.ofInstant(Instant.ofEpochMilli(nextChargeAttempt.getTime()), ZoneId.of("GMT"));
	}

	public void setNextChargeAttempt(final ZonedDateTime next) {
		this.nextChargeAttempt = DateTimeUtils.toDate(next.toInstant());
	}
}
