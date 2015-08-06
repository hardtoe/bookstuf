package com.bookstuf.web;

import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;

import static com.googlecode.objectify.ObjectifyService.ofy;

import com.bookstuf.GsonHelper;
import com.bookstuf.appengine.HandleToProfessionalInformationKeyMemcacheable;
import com.bookstuf.appengine.NotLoggedInException;
import com.bookstuf.appengine.StripeApi;
import com.bookstuf.appengine.StripeApiBase;
import com.bookstuf.appengine.UserManager;
import com.bookstuf.datastore.Booking;
import com.bookstuf.datastore.ConsumerInformation;
import com.bookstuf.datastore.ConsumerDailyAgenda;
import com.bookstuf.datastore.DailyAgenda;
import com.bookstuf.datastore.ProfessionalPrivateInformation;
import com.bookstuf.datastore.ProfessionalInformation;
import com.bookstuf.datastore.Service;
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.identitytoolkit.GitkitClientException;
import com.google.identitytoolkit.GitkitUser;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Result;
import com.stripe.model.Customer;
import com.stripe.model.Refund;

@Singleton
@SuppressWarnings("serial")
public class BookingServlet extends RpcServlet {
	private final Logger logger;
	private final UserManager userService;
	private final GsonHelper gsonHelper;
	private final HandleToProfessionalInformationKeyMemcacheable handleToUserInformationKey;
	private final Provider<GitkitUser> gitkitUser;
	private final StripeApi stripe;
	
	@Inject BookingServlet(
		final Logger logger,
		final UserManager userService,
		final GsonHelper gsonHelper,
		final HandleToProfessionalInformationKeyMemcacheable handleToUserInformationKey,
		final Provider<GitkitUser> gitkitUser,
		final StripeApi stripe
	) {
		this.logger = logger;
		this.userService = userService;
		this.gsonHelper = gsonHelper;
		this.handleToUserInformationKey = handleToUserInformationKey;
		this.gitkitUser = gitkitUser;
		this.stripe = stripe;
	}

	@Default
	private void notFound(final HttpServletResponse response) {
		response.setStatus(404);
	}
	
	public static class BookingRequest {
		public String professionalUserId;
		public Service service;
		public LocalDate date;
		public LocalTime startTime;
		
		public boolean isNewStripeCustomer;
		public String stripeToken;
	}
	
	// TODO: initiate charge request for user
	// TODO: handle cash-only transactions and add charge to professional account
	// TODO: use custom retry and transaction code
	@Publish(withAutoRetryMillis = 30000) @AsTransaction
	private String book(
		@RequestBody final BookingRequest request
	) {	
		// FIXME: use idempotent keys for stripe requests
		
		// create customer with stripe if necessary
		ListenableFuture<Customer> newStripeCustomerFuture;

		if (request.isNewStripeCustomer) {
			newStripeCustomerFuture = 
				stripe.customer().create().source(request.stripeToken).send();
		}
		
		// consumer information
		final String consumerUserId =
			gitkitUser.get().getLocalId();
		
		final Key<ConsumerInformation> consumerKey = 
			Key.create(ConsumerInformation.class, consumerUserId);
		
		final Result<ConsumerInformation> consumerInformationResult =
			userService.getCurrentConsumerInformation();
		
		// daily agendas
		final Key<ProfessionalInformation> professionalKey = 
			Key.create(ProfessionalInformation.class, request.professionalUserId);
		
		// these datastore requests are ordered this way to maximize parallelism
		final Result<DailyAgenda> professionalDailyAgendaResult =
			getProfessionalDailyAgenda(request.professionalUserId, request.date);
		
		final Result<ConsumerDailyAgenda> consumerDailyAgendaResult =
			getConsumerDailyAgenda(consumerKey, consumerUserId, request.date);
	
		// extract the booking from the result
		final Booking requestedBooking =
			new Booking();
		
		requestedBooking.setProfessional(professionalKey);
		requestedBooking.setConsumer(consumerKey);
		requestedBooking.setService(request.service);
		requestedBooking.setStartTime(request.startTime);
		
		// get the daily agendas ready
		final DailyAgenda professionalDailyAgenda =
			professionalDailyAgendaResult.now();
		
		final ConsumerDailyAgenda consumerDailyAgenda =
			consumerDailyAgendaResult.now();
		
		// save new customer id to the datastore
		if (request.isNewStripeCustomer) {
			final ConsumerInformation consumerInformation =
				consumerInformationResult.now();
			
			final Customer newStripeCustomer =
				newStripeCustomerFuture.get();
			
			// FIXME: check for and handle bad response from stripe (like invalid card) and rollback transaction if needed
			
			consumerInformation.setStripeCustomerId(newStripeCustomer.getId());

			ofy().save().entity(consumerInformation);
		}
		
		// see if we can make the booking...
		if (
			professionalDailyAgenda.canAdd(requestedBooking) &&
			consumerDailyAgenda.canAdd(requestedBooking)
		) {
			// ...make it if we can...
			professionalDailyAgenda.add(requestedBooking);
			consumerDailyAgenda.add(requestedBooking);

			ofy().save().entity(professionalDailyAgenda);
			
			// consumer daily agenda is used to remember that the card needs to be charged
			ofy().save().entity(consumerDailyAgenda);
			
			// ...and report success
			return "{\"success\": true}";
			
		} else {
			ofy().getTransaction().rollbackAsync();
			
			// ...report failure if we can't
			return "{\"alreadyBooked\": true}";
		}
	}

	private Result<DailyAgenda> getProfessionalDailyAgenda(
		final String professionalUserId, 
		final LocalDate date
	) {
		final Key<DailyAgenda> key =
			DailyAgenda.createKey(professionalUserId, date);

		return new Result<DailyAgenda>() {
			@Override
			public DailyAgenda now() {
				try {
					return ofy().load().key(key).safe();
					
				} catch (final NotFoundException e) {
					final DailyAgenda dailyAgenda =
						new DailyAgenda();
					
					dailyAgenda.setOwnerAndDate(professionalUserId, date);
					
					return dailyAgenda;
				}
			}		
		};
	}

	private Result<ConsumerDailyAgenda> getConsumerDailyAgenda(
		final Key<ConsumerInformation> consumerKey, 
		final String consumerUserId, 
		final LocalDate date
	) {
		final Key<ConsumerDailyAgenda> key =
			ConsumerDailyAgenda.createKey(consumerKey, consumerUserId, date);
		
		return new Result<ConsumerDailyAgenda>() {
			@Override
			public ConsumerDailyAgenda now() {
				try {
					return ofy().load().key(key).safe();
					
				} catch (final NotFoundException e) {
					final ConsumerDailyAgenda dailyAgenda =
						new ConsumerDailyAgenda();
					
					dailyAgenda.setOwnerAndDate(consumerUserId, date);
					
					return dailyAgenda;
				}
			}	
		};
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
