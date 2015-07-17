package com.bookstuf.web;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bookstuf.appengine.NotLoggedInException;
import com.bookstuf.appengine.UserService;
import com.bookstuf.datastore.User;
import com.bookstuf.datastore.UserMeta;
import com.google.identitytoolkit.GitkitClient;
import com.google.identitytoolkit.GitkitClientException;
import com.google.identitytoolkit.GitkitUser;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
@SuppressWarnings("serial")
public class LoginServlet extends HttpServlet {
	private final Logger logger;
	private final Provider<GitkitClient> gitkitClient;
	private final UserService userService;
	
	@Inject LoginServlet(
		final Logger logger,
		final Provider<GitkitClient> gitkitClient,
		final UserService userService
	) {
		this.logger = logger;
		this.gitkitClient = gitkitClient;
		this.userService = userService;
	}
	
	@Override
	protected void doGet(
		final HttpServletRequest request,
		final HttpServletResponse response
	) throws
		ServletException, 
		IOException 
	{
		try {
			final GitkitUser gitkitUser =
				gitkitClient.get().validateTokenInRequest(request);
			
			if (gitkitUser == null) {
				response.addCookie(new Cookie("gtoken", ""));
				logger.log(Level.WARNING, "LoginServlet accessed with invalid gtoken.");
			}
			
		} catch (final GitkitClientException e) {
			logger.log(Level.SEVERE, "Could not validate gitkit user.", e);
		}
		
		response.setStatus(302);
		response.addHeader("Location", getCookie(request, "destination"));
		response.addCookie(new Cookie("destination", ""));
	}
	
	private String getCookie(
		final HttpServletRequest req, 
		final String name
	) {
		for (final Cookie c : req.getCookies()) {
			if (c.getName().equals(name)) {
				return c.getValue();
			}
		}
		
		return "";
	}
}
