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
	private final Provider<GitkitClient> gitkitClient;
	private final UserService userService;
	private final GsonHelper gsonHelper;
	
	@Inject UserServlet(
		final Logger logger,
		final Provider<GitkitClient> gitkitClient,
		final UserService userService,
		final GsonHelper gsonHelper
	) {
		this.logger = logger;
		this.gitkitClient = gitkitClient;
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
		IOException, 
		NotLoggedInException 
	{
		final GitkitUser gitkitUser =
			gitkitClient.get().validateTokenInRequest(request);

		return 
			userService.getCurrentUser(gitkitUser, null);
	}
	
	@Publish(autoRetryMillis = 30000)
	private void setUserInformation(
		final HttpServletRequest request
	) throws 
		IOException, 
		GitkitClientException, 
		NotLoggedInException 
	{
		final GitkitUser gitkitUser =
			gitkitClient.get().validateTokenInRequest(request);

		final Transaction t =
			Datastore.beginTransaction();
			
		try {					
			final UserInformation userInformation =
				gsonHelper.updateFromJson(
						request.getReader(),
					userService.getCurrentUserInformation(gitkitUser, t));

			// TODO: handle errors
			Datastore.put(userInformation);
			
			t.commit();
			
		} finally { 
			if (t.isActive()) {
				t.rollback();
			}
		}
	}
	
	@Publish(autoRetryMillis = 30000)
	private UserInformation getUserInformation(
		final HttpServletRequest request
	) throws 
		IOException, 
		GitkitClientException, 
		NotLoggedInException 
	{
		final GitkitUser gitkitUser =
			gitkitClient.get().validateTokenInRequest(request);
	
		return 
			userService.getCurrentUserInformation(gitkitUser, null);	
	}
	
	@Publish(autoRetryMillis = 30000)
	private void setUserServices(
		final HttpServletRequest request
	) throws 
		IOException, 
		GitkitClientException, 
		NotLoggedInException 
	{
		final GitkitUser gitkitUser =
			gitkitClient.get().validateTokenInRequest(request);

		final Transaction t =
			Datastore.beginTransaction();
			
		try {					
			final UserServices userServices =
				gsonHelper.updateFromJson(
					request.getReader(),
					userService.getCurrentUserServices(gitkitUser, t));

			// TODO: handle errors
			Datastore.put(userServices);
			
			t.commit();
			
		} finally { 
			if (t.isActive()) {
				t.rollback();
			}
		}
	}
	
	@Publish(autoRetryMillis = 30000)
	private UserServices getUserServices(
		final HttpServletRequest request
	) throws 
		IOException, 
		GitkitClientException, 
		NotLoggedInException 
	{
		final GitkitUser gitkitUser =
			gitkitClient.get().validateTokenInRequest(request);

		return
			userService.getCurrentUserServices(gitkitUser, null);
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
