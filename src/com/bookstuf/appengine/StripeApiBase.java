package com.bookstuf.appengine;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.net.RequestOptions;
import com.stripe.net.RequestOptions.RequestOptionsBuilder;

// TODO: retry failed requests?

public abstract class StripeApiBase {
	private Provider<ListeningExecutorService> execService;

	@Inject protected void setKeyStore(final KeyStore keyStore) {
		Stripe.apiKey = keyStore.getStripeClientSecret();
	}
	
	@Inject protected void setExecService(final Provider<ListeningExecutorService> execService) {
		this.execService = execService;
	}
	
	/**
	 * Base class for all Stripe API method calls.  Handles RequestOptions
	 * and building parameters in a fluent-style API.
	 * 
	 * @param <R> Return value from API call.  Use Void if none.
	 * @param <T> Type of class extending Method.
	 */
	protected abstract class Method<R, T extends Method<R, T>> {
		private final HashMap<String, Object> params = 
			new HashMap<String, Object>();

		private final RequestOptionsBuilder optionsBuilder = 
			RequestOptions.builder();

		/**
		 * Special request options for API call.
		 */
		protected final RequestOptions options() {
			return optionsBuilder.build();
		}

		/**
		 * Set idempotency key to be used if the same request needs to
		 * be retried but you are unsure if Stripe has already received
		 * the request or not.
		 */
		@SuppressWarnings("unchecked")
		public final T idempotencyKey(final String key) {
			optionsBuilder.setIdempotencyKey(key);
			return (T) this;
		}

		/**
		 * Set a parameter to be sent in the API call.
		 */
		@SuppressWarnings("unchecked")
		protected final T set(final String key, final Object value) {
			params.put(key, value);
			return (T) this;
		}

		/**
		 * Parameters for API call.
		 */
		protected final HashMap<String, Object> params() {
			return params;
		}

		/**
		 * Send the request to Stripe's servers immediately and return the
		 * response.
		 */
		public abstract R get() throws StripeException;

		/**
		 * Send the request to Stripe's servers asynchronously.
		 */
		public final ListenableFuture<R> send() {
			return execService.get().submit(new Callable<R>() {
				@Override
				public R call() throws Exception {
					return get();
				}
			});
		}
		
		protected final HashMap<String, String> map(final String... pairs) {
			if (pairs == null) {
				return null;
				
			} else {
				final HashMap<String, String> map =
					new HashMap<String, String>();
				
				for (int i = 0; i < pairs.length; i += 2) {
					map.put(pairs[i], pairs[i + 1]);
				}
				
				return map;
			}
		}
	}
}
