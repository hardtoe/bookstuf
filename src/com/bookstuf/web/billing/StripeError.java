package com.bookstuf.web.billing;

import java.io.IOException;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLHandshakeException;

import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;

public class StripeError {
	public static enum Type {
		//					email admin,		long retriable,		short retriable,		needs new card		use new nonce		
		INVALID_REQUEST(	true,				true,				false,					false,				true),
		INVALID_CARD(		false,				false,				false,					true,				true),
		CARD_DECLINED(		false,				true,				false,					true,				true),
		PROCESSING_ERROR(	true,				true,				true,					false,				true),
		RATE_LIMIT_ERROR(	true,				true,				false,					false,				true),
		MISSING_ERROR(		true,				false,				false,					true,				true),
		STRIPE_ERROR(		true,				true,				true,					false,				true),
		NETWORK_ERROR(		false,				true,				true,					false,				false),
		INTERNAL_ERROR(		true,				true,				false,					false,				false);

		private final boolean emailAdmin;
		private final boolean longRetriable;
		private final boolean shortRetriable;
		private final boolean needsNewCard;
		private final boolean useNewNonce;

		private Type(
			final boolean emailAdmin,
			final boolean longRetriable,
			final boolean shortRetriable,
			final boolean needsNewCard,
			final boolean useNewNonce
		) {
			this.emailAdmin = emailAdmin;
			this.longRetriable = longRetriable;
			this.shortRetriable = shortRetriable;
			this.needsNewCard = needsNewCard;
			this.useNewNonce = useNewNonce;
		}

		private static Type fromThrowable(final Throwable t) {
			if (t instanceof APIException) {
				return Type.STRIPE_ERROR;
				
			} else if (t instanceof InvalidRequestException) {
				return Type.INVALID_REQUEST;
				
			} else if (t instanceof AuthenticationException) {
				return Type.INVALID_REQUEST;
				
			} else if (t instanceof CardException) {	
				final CardException e =
					(CardException) t;
				
				if ("card_declined".equals(e.getCode())) {
					return Type.CARD_DECLINED;
					
				} else if ("processing_error".equals(e.getCode())) {
					return Type.PROCESSING_ERROR;
					
				} else if ("rate_limit".equals(e.getCode())) {
					return Type.RATE_LIMIT_ERROR;
					
				} else if ("missing".equals(e.getCode())) {
					return Type.MISSING_ERROR;
					
				} else {
					return Type.INVALID_CARD;
				}
				
			} else if (
				t instanceof SSLHandshakeException ||
				t instanceof SocketTimeoutException ||
				t instanceof IOException
			) {
				return Type.NETWORK_ERROR;
				
			} else {
				return Type.INTERNAL_ERROR;
			}
		}

		/**
		 * @return the emailAdmin
		 */
		public boolean isEmailAdmin() {
			return emailAdmin;
		}

		/**
		 * @return the longRetriable
		 */
		public boolean isLongRetriable() {
			return longRetriable;
		}

		/**
		 * @return the shortRetriable
		 */
		public boolean isShortRetriable() {
			return shortRetriable;
		}

		/**
		 * @return the needsNewCard
		 */
		public boolean isNeedsNewCard() {
			return needsNewCard;
		}

		/**
		 * @return the useNewNonce
		 */
		public boolean isUseNewNonce() {
			return useNewNonce;
		}
	}

	private final Type type;
	private final Throwable throwable;
	private final String userFriendlyMessage;
	
	public StripeError(
		final Throwable throwable
	) {
		this.type = Type.fromThrowable(throwable);
		this.throwable = throwable;
		this.userFriendlyMessage = throwable.getMessage();
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @return the throwable
	 */
	public Throwable getThrowable() {
		return throwable;
	}

	/**
	 * @return the userFriendlyMessage
	 */
	public String getUserFriendlyMessage() {
		return userFriendlyMessage;
	}
}
