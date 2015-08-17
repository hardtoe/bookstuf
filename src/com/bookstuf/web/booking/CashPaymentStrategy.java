package com.bookstuf.web.booking;

import java.math.BigDecimal;

import com.bookstuf.appengine.RetryHelper;
import com.bookstuf.appengine.StripeApi;
import com.bookstuf.appengine.UserManager;
import com.bookstuf.datastore.Booking;
import com.google.common.util.concurrent.ListeningExecutorService;

public class CashPaymentStrategy extends PaymentStrategy {
	private final BookingRequest request;
	private final Booking booking;
	private final UserManager userService;
	private final ListeningExecutorService execService;
	private final StripeApi stripe;
	private final RetryHelper retryHelper;

	public CashPaymentStrategy(
		final BookingRequest request, 
		final Booking booking,
		final UserManager userService,
		final ListeningExecutorService execService,
		final StripeApi stripe,
		final RetryHelper retryHelper
	) {
		this.request = request;
		this.booking = booking;
		this.userService = userService;
		this.execService = execService;
		this.stripe = stripe;
		this.retryHelper = retryHelper;
	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isPaymentLikely() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void execute(BigDecimal cost) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void rollbackPayment() {
		// TODO Auto-generated method stub
		
	}

}
