package com.bookstuf.web;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.googlecode.objectify.ObjectifyService.ofy;

import com.bookstuf.GsonHelper;
import com.bookstuf.appengine.NotLoggedInException;
import com.bookstuf.appengine.UserService;
import com.bookstuf.datastore.User;
import com.bookstuf.datastore.UserInformation;
import com.google.appengine.api.datastore.Transaction;
import com.google.identitytoolkit.GitkitClientException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@SuppressWarnings("serial")
public class UserServlet extends RpcServlet {
	private final Logger logger;
	private final UserService userService;
	private final GsonHelper gsonHelper;
	
	@Inject UserServlet(
		final Logger logger,
		final UserService userService,
		final GsonHelper gsonHelper
	) {
		this.logger = logger;
		this.userService = userService;
		this.gsonHelper = gsonHelper;
	}

	@Default
	private void notFound(final HttpServletResponse response) {
		response.setStatus(404);
	}
	
	@Publish(withAutoRetryMillis = 30000)
	private User getCurrentUser(
		final HttpServletRequest request
	) throws 
		GitkitClientException, 
		IOException
	{
		return userService.getCurrentUser();
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
		final UserInformation userInformation =
			gsonHelper.updateFromJson(
				request.getReader(),
				userService.getCurrentUserInformation());

		ofy().save().entity(userInformation);

		final User user =
			userService.getCurrentUser();
		
		user.setProviderInformationStatus(userInformation.getInformationStatus());
		user.setProviderServicesStatus(userInformation.getServicesStatus());
		
		ofy().save().entity(user);
		
		return "{}";
	}
	
	@Publish(withAutoRetryMillis = 30000)
	private UserInformation getUserInformation(
		final HttpServletRequest request
	) throws 
		IOException, 
		GitkitClientException
	{
		return userService.getCurrentUserInformation();	
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
