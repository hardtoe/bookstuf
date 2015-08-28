package com.bookstuf.web;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bookstuf.appengine.UserManager;
import com.bookstuf.datastore.ProfessionalInformation;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class ProfilePageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final Provider<UserManager> userService;
	private final Logger logger;

	private MustacheLoader m;

	@Inject ProfilePageServlet(
		final Provider<UserManager> userService,
		final Logger logger
	) {
		this.userService = userService;
		this.logger = logger;
	}
	
	@Override
	public void init() throws ServletException {
		this.m = new MustacheLoader(getServletContext());
	}
	
	@Override
	public void doGet(
		final HttpServletRequest req, 
		final HttpServletResponse rsp
	) throws 
		IOException 
	{
		logger.info("getServletPath(): " + req.getServletPath());
		
		final String handle =
			req.getServletPath().substring(1);
		
		final ProfessionalInformation userInformation =
			userService.get().getProfessionalInformationByHandle(handle);
		
		if (userInformation != null) {
		    m.load("ServicesPage.html").execute(rsp.getWriter(), userInformation);
		}
	}
}
