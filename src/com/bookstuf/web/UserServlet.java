package com.bookstuf.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slim3.datastore.Datastore;

import com.bookstuf.GsonHelper;
import com.bookstuf.appengine.NotLoggedInException;
import com.bookstuf.appengine.UserService;
import com.bookstuf.datastore.User;
import com.bookstuf.datastore.UserInformation;
import com.bookstuf.datastore.UserServices;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import com.google.identitytoolkit.GitkitClient;
import com.google.identitytoolkit.GitkitClientException;
import com.google.identitytoolkit.GitkitUser;
import com.google.inject.Inject;
import com.google.inject.Provider;
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
	
	@Publish(autoRetryMillis = 30000)
	private User getCurrentUser(
		final HttpServletRequest request
	) throws 
		GitkitClientException, 
		IOException
	{
		return userService.getCurrentUser(null);
	}
	
	@Publish(autoRetryMillis = 30000)
	private String setUserInformation(
		final HttpServletRequest request
	) throws 
		IOException, 
		GitkitClientException
	{
		final Transaction t =
			Datastore.beginTransaction();
			
		try {					
			final UserInformation userInformation =
				gsonHelper.updateFromJson(
					request.getReader(),
					userService.getCurrentUserInformation(t));

			// TODO: handle errors
			Datastore.put(userInformation);
			
			t.commit();
			
		} finally { 
			if (t.isActive()) {
				t.rollback();
			}
		}
		
		return "{}";
	}
	
	@Publish(autoRetryMillis = 30000)
	private UserInformation getUserInformation(
		final HttpServletRequest request
	) throws 
		IOException, 
		GitkitClientException
	{
		return userService.getCurrentUserInformation(null);	
	}
	
	@Publish(autoRetryMillis = 30000)
	private String setUserServices(
		final HttpServletRequest request
	) throws 
		IOException, 
		GitkitClientException, 
		NotLoggedInException 
	{
		final Transaction t =
			Datastore.beginTransaction();
			
		try {					
			final User user =
				userService.getCurrentUser(t);
			
			final UserServices userServices =
				gsonHelper.updateFromJson(
					request.getReader(),
					userService.getCurrentUserServices(t));

			// TODO: handle errors
			Datastore.put(userServices);
			Datastore.put(user);
			
			t.commit();
			
		} finally { 
			if (t.isActive()) {
				t.rollback();
			}
		}
		
		return "{}";
	}
	
	@Publish(autoRetryMillis = 30000)
	private UserServices getUserServices(
		final HttpServletRequest request
	) throws 
		IOException, 
		GitkitClientException
	{
		return userService.getCurrentUserServices(null);
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
