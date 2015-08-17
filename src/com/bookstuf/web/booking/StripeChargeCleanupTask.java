package com.bookstuf.web.booking;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bookstuf.appengine.DevKeyStore;
import com.bookstuf.appengine.StripeApi;
import com.bookstuf.datastore.Booking;
import com.google.appengine.api.ThreadManager;
import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.DeferredTaskContext;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.servlet.RequestScoped;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.ChargeCollection;
import com.stripe.model.Customer;
import com.stripe.model.Refund;

public class StripeChargeCleanupTask implements DeferredTask {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(StripeChargeCleanupTask.class.getCanonicalName());
	
	private Booking booking;

	public StripeChargeCleanupTask() {
		// needed for serialization?
	}
	
	public StripeChargeCleanupTask(final Booking booking) {
		this.booking = booking;
	}

	@Override
	public void run() {
		log.log(Level.INFO, "starting StripeChargeCleanupTask");
		try {			
			final StripeApi stripe =
				new StripeApi();
			
			stripe.setKeyStore(new DevKeyStore());
			
			if (booking.getStripeChargeId() != null) {
				final Charge charge =
					stripe.charge().retrieve(booking.getStripeChargeId()).get();
				
				refund(stripe, charge);
				
			} else if (booking.getStripeCustomerId() != null) {
				final ChargeCollection charges = 
					stripe.charge().all().customer(booking.getStripeCustomerId()).get();
				
				final Charge charge = 
					findCharge(200, stripe, charges);
				
				if (charge != null) {
					refund(stripe, charge);
				}
			}
		} catch (final Throwable t) {
			log.log(Level.SEVERE, "unable to clean stripe charge", t);
		}
	}

	private Charge findCharge(
		final int limit,
		final StripeApi stripe, 
		final ChargeCollection charges
	) throws 
		Exception
	{
		String lastChargeId = null;
		
		for (final Charge c : charges.getData()) {
			if (booking.getId().equals(c.getMetadata().get("bookstuf.booking.id"))) {
				return c;
				
			} else {
				lastChargeId = c.getId();
			}
		}
		
		if (limit > 0) {
			return findCharge(
				limit - charges.getCount(),
				stripe,
				stripe.charge().all().customer(booking.getStripeCustomerId()).startingAfter(lastChargeId).get());
			
		} else {
			return null;
		}
	}
	
	private void refund(
		final StripeApi stripe, 
		final Charge charge
	) throws 
		StripeException 
	{
		if (!charge.getRefunded()) {
			stripe.refund()
				.create(charge)
				.metadata("reason", "bookstuf server was unable to guarantee booking was made due to datastore errors")
				.get();
		}
	}

}
