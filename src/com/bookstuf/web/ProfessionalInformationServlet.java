package com.bookstuf.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;

import static com.googlecode.objectify.ObjectifyService.ofy;

import com.bookstuf.GsonHelper;
import com.bookstuf.appengine.HandleToProfessionalInformationKeyMemcacheable;
import com.bookstuf.appengine.NotLoggedInException;
import com.bookstuf.appengine.UserManager;
import com.bookstuf.datastore.Availability;
import com.bookstuf.datastore.ConsumerDailyAgenda;
import com.bookstuf.datastore.DailyAgenda;
import com.bookstuf.datastore.ProfessionalPrivateInformation;
import com.bookstuf.datastore.ProfessionalInformation;
import com.bookstuf.web.booking.PublicDailyAvailability;
import com.google.identitytoolkit.GitkitClientException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;

@Singleton
@SuppressWarnings("serial")
public class ProfessionalInformationServlet extends RpcServlet {
	private final Logger logger;
	private final UserManager userService;
	private final GsonHelper gsonHelper;
	private final HandleToProfessionalInformationKeyMemcacheable handleToUserInformationKey;
	
	@Inject ProfessionalInformationServlet(
		final Logger logger,
		final UserManager userService,
		final GsonHelper gsonHelper,
		final HandleToProfessionalInformationKeyMemcacheable handleToUserInformationKey
	) {
		this.logger = logger;
		this.userService = userService;
		this.gsonHelper = gsonHelper;
		this.handleToUserInformationKey = handleToUserInformationKey;
	}

	@Default
	private void notFound(final HttpServletResponse response) {
		response.setStatus(404);
	}
	
	@Publish(withAutoRetryMillis = 30000)
	private ProfessionalPrivateInformation getCurrentUser(
		final HttpServletRequest request
	) throws 
		GitkitClientException, 
		IOException
	{
		return userService.getCurrentProfessionalPrivateInformation();
	}
	
	@Publish(withAutoRetryMillis = 30000) @AsTransaction
	private String setUserInformation(
		final HttpServletRequest request
	) throws 
		IOException, 
		GitkitClientException, 
		InterruptedException, 
		ExecutionException
	{			
		final ProfessionalInformation userInformation =
			gsonHelper.updateFromJson(
				request.getReader(),
				userService.getCurrentProfessionalInformation());

		ofy().save().entity(userInformation);

		final ProfessionalPrivateInformation user =
			userService.getCurrentProfessionalPrivateInformation();
		
		user.setProviderInformationStatus(userInformation.getInformationStatus());
		user.setProviderServicesStatus(userInformation.getServicesStatus());
		
		ofy().save().entity(user);

		handleToUserInformationKey.updateCache(
			userInformation.getHandle(), 
			Key.create(ProfessionalInformation.class, userInformation.getGitkitUserId()));
		
		return "{}";
	}
	
	@Publish
	private ProfessionalInformation getUserInformationWithId(
		@Param("professionalUserId") final String professionalUserId
	) {
		final Key<ProfessionalInformation> professionalKey = 
			Key.create(ProfessionalInformation.class, professionalUserId);
		
		return 
			ofy().load().key(professionalKey).now();
	}
	
	@Publish(withAutoRetryMillis = 30000)
	private ProfessionalInformation getUserInformation(
		final HttpServletRequest request
	) throws 
		IOException, 
		GitkitClientException
	{
		return userService.getCurrentProfessionalInformation();	
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
