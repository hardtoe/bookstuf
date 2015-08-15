package com.bookstuf.web.billing;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import com.bookstuf.StateMachine;
import com.bookstuf.datastore.Bill;
import com.bookstuf.datastore.PaymentStatus;
import com.google.inject.Singleton;

import static com.bookstuf.datastore.PaymentStatus.*;

@Singleton
public class BillingStateMachine /*extends StateMachine<Bill, PaymentStatus, BillingEvent> */{
//	public void definition() {
//		arc(PENDING,				chargeAction,			withinHoursBeforeAppointmentEnd(0));
//		arc(chargeAction,			PAID,					stripeSuccess());
//		arc(chargeAction,			declinedEmailAction,	invalidCard());
//		arc(chargeAction,			PENDING,				stripeShouldRetry());
//		
//		arc(PENDING,				authorizeAction,		withinHoursBeforeAppointmentEnd(36));
//		arc(authorizeAction,		AUTHORIZED,				stripeSuccess());
//		arc(authorizeAction,		declinedEmailAction,	invalidCard());
//		arc(authorizeAction,		PENDING,				stripeShouldRetry());
//		
//		arc(AUTHORIZED,				captureAction,			withinHoursBeforeAppointmentEnd(0));
//		arc(captureAction,			PAID,					stripeSuccess());
//		arc(captureAction,			declinedEmailAction,	invalidCard());
//		arc(captureAction,			AUTHORIZED,				stripeShouldRetry());
//		
//		arc(declinedEmailAction,	DECLINED);
//		
//		arc(DECLINED,				updateCard,				updateCardEvent());
//		arc(updateCard,				PENDING);
//		
//		arc(DECLINED,				PENDING_CASH,			useCashEvent());
//		arc(PENDING_CASH,			invoiceFeeAction,		withinHoursBeforeAppointmentEnd(0));
//		arc(invoiceFeeAction,		FEE_INVOICED_CASH);
//		arc(FEE_INVOICED_CASH,		PAID,					cashPaidEvent());
//	}

//	@Override
//	protected PaymentStatus getCurrentState(
//		final Bill context
//	) {
//		return context.getBooking().getPaymentStatus();
//	}
//	
//	@Override
//	protected void setCurrentState(
//		final Bill context, 
//		final PaymentStatus state
//	) {
//		context.getBooking().setPaymentStatus(state);
//	}
//
//	private final Predicate withinHoursBeforeAppointmentEnd(
//		final int hoursBeforeAppointmentEnd
//	) {
//		return new Predicate() {
//			@Override
//			public boolean isTrue(
//				final Bill context, 
//				final BillingEvent event
//			) {
//				final LocalDateTime now =
//					LocalDateTime.now(ZoneId.of("America/Los_Angeles"));
//				
//				final LocalDateTime deadline =
//					context.getNextChargeAttempt().minusHours(hoursBeforeAppointmentEnd);
//				
//				return now.isAfter(deadline);
//			}
//		};
//	}
//
//	private final Predicate stripeSuccess() {
//		return new Predicate() {
//			
//		};
//	}
//
//	/*
//	 * Retriable stripe exceptions due to card issue (must not be idempotent):
//	 *     CardException							- something wrong with the credit card
//	 *         card_declined						- declined...could be many different reasons
//	 *         
//	 *         processing_error						- some vague error occured while processing, should be retriable
//	 *         rate_limit							- need to email me about rate limit error
//	 * 
//	 * Retriable stripe exceptions due to technical error (must be idempotent):
//	 *     java.net.SocketTimeoutException 			- If the request takes too long to respond.
//	 *     java.io.IOException 						- If the remote service could not be contacted or the URL could not be fetched.
//	 * 
//	 * Major stripe errors that should be reported to developer via email (and retried):
//	 *     InvalidRequestException					- Bad API parameters being sent
//	 *     AuthenticationException					- Bad keys being sent to stripe
//	 *     APIException 							- Major API failure
//	 *     RequestPayloadTooLargeException 			- If the provided payload exceeds the limit.
//	 *     ResponseTooLargeException 				- If the response is too large.
//	 *     javax.net.ssl.SSLHandshakeException 		- If the server's SSL certificate could not be validated and validation was requested.
//	 */
//	private final Predicate stripeShouldRetry() {
//		return new Predicate() {
//			
//		};
//	}
//
//	private final Predicate invalidCard() {
//		return new Predicate() {
//			
//		};
//	}
//
//	private final Predicate updateCardEvent() {
//		return new Predicate() {
//			
//		};
//	}
//
//	private final Predicate useCashEvent() {
//		return new Predicate() {
//			
//		};
//	}
//
//	private final Predicate cashPaidEvent() {
//		return new Predicate() {
//			
//		};
//	}
//	
//	private final Action chargeAction =
//		new Action() {
//
//			@Override
//			public BillingEvent run(
//				final Bill context, 
//				BillingEvent event
//			) {
//				// TODO Auto-generated method stub
//				return null;
//			}
//		
//		};
//
//	private final Action declinedEmailAction =
//		new Action() {
//		
//		};
//
//	private final Action authorizeAction =
//		new Action() {
//		
//		};
//
//	private final Action captureAction =
//		new Action() {
//		
//		};
//
//	private final Action updateCard =
//		new Action() {
//		
//		};
//
//	private final Action invoiceFeeAction =
//		new Action() {
//		
//		};
	
}
