package com.bookstuf.web;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.servlet.http.*;

import com.bookstuf.appengine.RetryHelper;
import com.bookstuf.external.GitkitService;
import com.bookstuf.external.StripeService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.identitytoolkit.GitkitUser;

@Singleton
@SuppressWarnings("serial")
public class StripeServlet extends HttpServlet {
	private RetryHelper retryHelper;
	private StripeService stripeService;
	
	@Inject StripeServlet(
		final RetryHelper retryHelper, 
		final StripeService stripeService
	) {
		this.retryHelper = retryHelper;
		this.stripeService = stripeService;
	}
	
	public void doGet(
		final HttpServletRequest req, 
		final HttpServletResponse resp
	) throws IOException {
		final GitkitUser gitkitUser =
			GitkitService.getUser(getServletContext(), req);

		// TODO: handle error flow
		final String stripeAuthCode =
			req.getParameter("code");

		final URLFetchService urlFetchService =
			URLFetchServiceFactory.getURLFetchService();
		
		final HTTPRequest stripeTokenRequest = 
			new HTTPRequest(new URL("https://connect.stripe.com/oauth/token"), HTTPMethod.POST);
		
		stripeTokenRequest.setPayload(
			("client_id=ca_6alXrQKPz1whryd22S7nu6widUhQasz7&" +
			"client_secret=sk_test_NkEKdstqaaibV1iTBeH73mGC&" +
			"code=" + stripeAuthCode + "&" +
			"grant_type=authorization_code").getBytes());
		
		final Future<HTTPResponse> stripeTokenResponse = 
			urlFetchService.fetchAsync(stripeTokenRequest);

		resp.setContentType("text/plain");
		resp.getWriter().println("Hello, world");
		resp.getWriter().println("gitkit email: " + gitkitUser.getEmail());
		resp.getWriter().println("stripe auth code: " + stripeAuthCode);

		try {
			resp.getWriter().println("stripe token req response: " + new String(stripeTokenResponse.get().getContent()));
		} catch (InterruptedException | ExecutionException e) {
			resp.getWriter().println("stripe token req response: EXCEPTION");
			e.printStackTrace(resp.getWriter());
		}
	}
}
