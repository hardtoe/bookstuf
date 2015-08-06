package com.bookstuf.web;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.googlecode.objectify.ObjectifyService.ofy;

import com.bookstuf.GsonHelper;
import com.bookstuf.appengine.HandleToProfessionalInformationKeyMemcacheable;
import com.bookstuf.appengine.NotLoggedInException;
import com.bookstuf.appengine.UserManager;
import com.bookstuf.datastore.ProfessionalPrivateInformation;
import com.bookstuf.datastore.ProfessionalInformation;
import com.google.identitytoolkit.GitkitClientException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlecode.objectify.Key;

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
