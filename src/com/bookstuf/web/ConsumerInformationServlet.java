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
import com.bookstuf.appengine.UserManager;
import com.bookstuf.datastore.ConsumerInformation;
import com.google.identitytoolkit.GitkitClientException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@SuppressWarnings("serial")
public class ConsumerInformationServlet extends RpcServlet {
	private final Logger logger;
	private final UserManager userService;
	private final GsonHelper gsonHelper;
	
	@Inject ConsumerInformationServlet(
		final Logger logger,
		final UserManager userService,
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
	
	@Publish(withAutoRetryMillis = 30000) @AsTransaction
	private String setUserInformation(
		final HttpServletRequest request
	) throws 
		IOException, 
		GitkitClientException, 
		InterruptedException, 
		ExecutionException
	{			
		final ConsumerInformation userInformation =
			gsonHelper.updateFromJson(
				request.getReader(),
				userService.getCurrentConsumerInformation().now());

		ofy().save().entity(userInformation);
		
		return "{}";
	}
	
	@Publish(withAutoRetryMillis = 30000)
	private ConsumerInformation getUserInformation(
		final HttpServletRequest request
	) throws 
		IOException, 
		GitkitClientException
	{
		return userService.getCurrentConsumerInformation().now();	
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
