package com.bookstuf.web.card;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.Duration;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.TextStyle;

import static com.googlecode.objectify.ObjectifyService.ofy;

import com.bookstuf.GsonHelper;
import com.bookstuf.appengine.NotLoggedInException;
import com.bookstuf.appengine.RetryHelper;
import com.bookstuf.appengine.StripeApi;
import com.bookstuf.appengine.UserManager;
import com.bookstuf.datastore.Availability;
import com.bookstuf.datastore.Booking;
import com.bookstuf.datastore.ConsumerInformation;
import com.bookstuf.datastore.ConsumerDailyAgenda;
import com.bookstuf.datastore.DailyAgenda;
import com.bookstuf.datastore.PaymentMethod;
import com.bookstuf.datastore.PaymentStatus;
import com.bookstuf.datastore.ProfessionalInformation;
import com.bookstuf.datastore.Service;
import com.bookstuf.web.Default;
import com.bookstuf.web.ExceptionHandler;
import com.bookstuf.web.Param;
import com.bookstuf.web.Publish;
import com.bookstuf.web.RpcServlet;
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.identitytoolkit.GitkitClientException;
import com.google.identitytoolkit.GitkitUser;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Result;
import com.googlecode.objectify.Work;
import com.stripe.exception.StripeException;
import com.stripe.model.Card;
import com.stripe.model.Customer;

@Singleton
@SuppressWarnings("serial")
public class CardServlet extends RpcServlet {
	private final Logger logger;
	private final UserManager userService;
	private final GsonHelper gsonHelper;
	private final Provider<GitkitUser> gitkitUser;
	private final RetryHelper retryHelper;
	private final StripeApi stripe;
	private final Provider<ListeningExecutorService> execService;
	
	@Inject CardServlet(
		final Logger logger,
		final UserManager userService,
		final GsonHelper gsonHelper,
		final Provider<GitkitUser> gitkitUser,
		final RetryHelper retryHelper,
		final Provider<ListeningExecutorService> execService,
		final StripeApi stripe
	) {
		this.logger = logger;
		this.userService = userService;
		this.gsonHelper = gsonHelper;
		this.gitkitUser = gitkitUser;
		this.retryHelper = retryHelper;
		this.execService = execService;
		this.stripe = stripe;
	}

	@Default
	private void notFound(final HttpServletResponse response) {
		response.setStatus(404);
	}

	/**
	 *  this will only return the 10 cards on the customer object itself,
	 *  bookstuf needs to limit the number of cards that can be added
	 */
	@Publish(withAutoRetryMillis = 30000)
	private List<CardSummary> all() throws StripeException {
		final ArrayList<CardSummary> cards =
			new ArrayList<>();
		
		final ConsumerInformation consumerInformation =
			userService.getCurrentConsumerInformation().now();
		
		final String stripeCustomerId = 
			consumerInformation.getStripeCustomerId();
		
		if (stripeCustomerId != null) {
			final Customer customer =
				stripe.customer().retrieve(stripeCustomerId).get();
			
			final List<Card> stripeCards = 
				customer.getCards().getData();
			
			final String defaultCard =
				customer.getDefaultCard();
			
			for (final Card c : stripeCards) {
				cards.add(new CardSummary(
					c.getId(),
					c.getId().equals(defaultCard),
					c.getBrand(),
					c.getLast4(),
					c.getExpMonth(),
					c.getExpYear(),
					c.getMetadata().containsKey("deleted")
				));
			}
		}
		
		return cards;
	}
	
	@Publish
	private String add(
		@Param("token") final String token,
		@Param("setDefault") final String setDefault 
	) throws 
		Exception 
	{
		final String customerUpdateIdemKey = UUID.randomUUID().toString();
		final String cardCreateIdemKey = UUID.randomUUID().toString();
		
		return retryHelper.transactNew(30000, new Callable<String>() {
			@Override
			public String call() throws Exception {
				final ConsumerInformation consumerInformation =
					userService.getCurrentConsumerInformation().now();
				
				final Customer customer =
					stripe.customer().retrieve(consumerInformation.getStripeCustomerId()).get();

				if (customer.getCards().getTotalCount() < 10) {
					if ("true".equals(setDefault)) {
						stripe.customer()
							.update(customer)
							.source(token)
							.idempotencyKey(customerUpdateIdemKey)
							.get();
						
					} else {
						stripe.card()
							.create(customer, token)
							.idempotencyKey(cardCreateIdemKey)
							.get();
					}
					
					return "{\"success\": true}";
					
				} else {

					return "{\"error\": \"too many cards\"}";
				}
			}
		});
	}
	
	
	@ExceptionHandler(ExpiredCardException.class) 
	private void handleExpiredCardException(
		final HttpServletResponse response
	) throws 
		IOException 
	{
		response.getWriter().println("{\"unableToProcessCard\": true}");
	}
	
	@ExceptionHandler(DatastoreFailureException.class) 
	private void handleDatastoreFailureException(
		final HttpServletResponse response
	) throws 
		IOException 
	{
		response.getWriter().println("{\"tryAgain\": true}");
	}

	@ExceptionHandler(ConcurrentModificationException.class) 
	private void handleConcurrentModificationException(
		final HttpServletResponse response
	) throws 
		IOException 
	{
		response.getWriter().println("{\"tryAgain\": true}");
	}

	@ExceptionHandler(StripeException.class) 
	private void handleStripeException(
		final HttpServletResponse response
	) throws 
		IOException 
	{
		response.getWriter().println("{\"unableToProcessCard\": true}");
	}

	@ExceptionHandler(NotLoggedInException.class) 
	private void handleNotLoggedInException(
		final HttpServletResponse response
	) throws 
		IOException 
	{
		response.getWriter().println("{\"notLoggedIn\": true}");
	}
	
	@ExceptionHandler(GitkitClientException.class) 
	private void handleGitkitClientException(
		final HttpServletResponse response
	) throws 
		IOException 
	{
		logger.log(Level.SEVERE, "Could not validate gitkit user.", getCurrentException());
		response.getWriter().println("{\"notLoggedIn\": true}");
	}
}
