package com.bookstuf.web.booking;

import java.math.BigDecimal;

public abstract class PaymentStrategy {
	public abstract void prepare();
	public abstract boolean isPaymentLikely() throws Exception;
	public abstract void execute(final BigDecimal cost) throws Exception;
	public abstract void rollbackPayment();
}
