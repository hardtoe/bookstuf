package com.bookstuf.web.billing;

import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;

import com.bookstuf.appengine.StripeApi;
import com.bookstuf.datastore.Booking;
import com.bookstuf.datastore.DailyAgenda;
import com.bookstuf.datastore.PaymentMethod;
import com.bookstuf.datastore.PaymentStatus;
import com.bookstuf.datastore.ProfessionalInformation;
import com.bookstuf.datastore.ProfessionalPrivateInformation;
import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Work;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class BillDailyAgenda /* extends MapOnlyMapper<com.google.appengine.api.datastore.Key, Void> */ {
/*
	private static final Logger log = Logger.getLogger(BillDailyAgenda.class.getName());
	  
	@Override
	public void map(
		final com.google.appengine.api.datastore.Key dailyAgendaRawKey
	) {
		ofy().transactNew(0, new Work<Void>() {
			@Override
			public Void run() {
				final Key<DailyAgenda> dailyAgendaKey =
					Key.create(dailyAgendaRawKey);
				
				log.info("processing billing for " + dailyAgendaKey);
				
				final DailyAgenda agenda = 
					ofy().load().key(dailyAgendaKey).now();
				
				final Key<ProfessionalPrivateInformation> professionalPrivateKey =
					Key.create(ProfessionalPrivateInformation.class, agenda.getOwner());
				
				final ProfessionalPrivateInformation professionalPrivateInfo =
					ofy().transactionless().load().key(professionalPrivateKey).now();
				
				final String professionalStripeAccountId =
					professionalPrivateInfo.getStripeUserId();
				
				final LocalDate today = LocalDate.now();
				final LocalTime now = LocalTime.now();
	
				if (
					agenda.getDate().isBefore(today) ||
					agenda.getDate().isEqual(today)
				) {
					log.info("agenda passes date test");
					
					final StripeApi stripe =
						new StripeApi();
					
					for (final Booking booking : agenda.allBookings()) {
						final String bookingId =
							booking.getId();
						
						final LocalTime endTime =
							booking.getStartTime().plus(booking.getService().getDuration());
						
						final LocalDateTime bookingDateTime =
							LocalDateTime.of(agenda.getDate(), endTime);
						
						final LocalDateTime currentDateTime =
							LocalDateTime.now();
						
						log.info("working on booking id " + bookingId + " starting at " + booking.getStartTime());
						
						log.info(String.format("currentDateTime.isAfter(bookingDateTime), (%s).isAfter(%s) == %b", currentDateTime, bookingDateTime, currentDateTime.isAfter(bookingDateTime)));
						log.info(String.format("booking.getPaymentMethod() == %s, (booking.getPaymentMethod() == PaymentMethod.STRIPE_CARD) == %b", booking.getPaymentMethod(), booking.getPaymentMethod() == PaymentMethod.STRIPE_CARD));
						log.info(String.format("booking.getPaymentStatus() == %s, (booking.getPaymentStatus() == PaymentStatus.PENDING) == %b", booking.getPaymentStatus(), booking.getPaymentStatus() == PaymentStatus.PENDING));
							
						if (
							currentDateTime.isAfter(bookingDateTime) &&
							booking.getPaymentMethod() == PaymentMethod.STRIPE_CARD &&
							booking.getPaymentStatus() == PaymentStatus.PENDING
						) {
							log.info("booking passes date/time, payment method, and payment status test");

							if (currentDateTime.isAfter(bookingDateTime.plusHours(23))) {
								log.warning("extended idempotency key handling has been triggered");
								// TODO: check for idempotent key from stripe charge metadata			
							}
								
							final int amount = 
								booking.getService().getCost().multiply(new BigDecimal(100)).intValue();
							
							final int stripeFee =
								((amount * 29) / 1000) + 30;
							
							final int bookstufFee =
								amount / 100;
							
							log.info(String.format("amount: %d, stripe fee: %d, bookstuf fee: %d", amount, stripeFee, bookstufFee));
							
							try {
								final Charge charge =
									stripe.charge()
										.create(amount, "usd")
										.customer(booking.getStripeCustomerId())
										.source(booking.getStripeCardId())
										.destination(professionalStripeAccountId)
										.applicationFee(stripeFee + bookstufFee)
										.metadata("bookstuf.id", bookingId)
										.idempotencyKey(bookingId + " 2")
										.get();
								
								booking.setPaymentStatus(PaymentStatus.PAID);
								booking.setStripeChargeId(charge.getId());
								
								log.info("stripe charge succeeded");
								
							} catch (final StripeException e) {
								log.log(Level.WARNING, "stripe charge failed: ", e);
							}
						}
					}
				}
				
				ofy().save().entities(agenda);
				
				return null;
			}});
	}
	*/
}
