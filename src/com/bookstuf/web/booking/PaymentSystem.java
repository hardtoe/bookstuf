package com.bookstuf.web.booking;

import java.math.BigDecimal;

public abstract class PaymentSystem {
	public abstract void prepare();
	public abstract boolean isPaymentLikely();
	public abstract void execute(final BigDecimal cost);
	public abstract void throwErrorIfPresent();
}
