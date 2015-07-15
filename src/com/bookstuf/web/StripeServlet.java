package com.bookstuf.web;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.datastore.Key;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import javax.servlet.http.*;

import org.slim3.datastore.Datastore;

import com.bookstuf.appengine.NotLoggedInException;
import com.bookstuf.appengine.RetryHelper;
import com.bookstuf.appengine.UserService;
import com.bookstuf.datastore.StripeConnectStatus;
import com.bookstuf.datastore.User;
import com.bookstuf.datastore.UserMeta;
import com.bookstuf.external.StripeService;
import com.bookstuf.external.StripeService.StripeConnectAuthorizationResponse;
import com.google.identitytoolkit.GitkitClient;
import com.google.identitytoolkit.GitkitClientException;
import com.google.identitytoolkit.GitkitUser;

@Singleton
@SuppressWarnings("serial")
public class StripeServlet extends HttpServlet {
	private final Provider<RetryHelper> retryHelper;
	private final Provider<StripeService> stripeService;
	private final GitkitClient gitkitClient;
	private UserService userService;
	
	@Inject StripeServlet(
		final Provider<RetryHelper> retryHelper, 
		final Provider<StripeService> stripeService,
		final GitkitClient gitkitClient,
		final UserService userService
	) {
		this.retryHelper = retryHelper;
		this.stripeService = stripeService;
		this.gitkitClient = gitkitClient;
		this.userService = userService;
	}
	
	public void doGet(
		final HttpServletRequest req, 
		final HttpServletResponse resp
	) throws IOException {
		GitkitUser gitkitUser;
		
		try {
			gitkitUser = gitkitClient.validateTokenInRequest(req);
			
		} catch (GitkitClientException e) {
			// TODO: better way to handle this gitkit exception?
			throw new RuntimeException(e);
		}

		// TODO: handle error flow
		final String stripeAuthCode =
			req.getParameter("code");

		final Future<StripeConnectAuthorizationResponse> stripeConnectAccount =
			stripeService.get().connectAccountAsync(stripeAuthCode);
		
		// TODO: change this to the correct location or embed it into the stripe state
		resp.setStatus(302);
		resp.addHeader("Location", "https://www.bookstuf.com/provider-checklist.html");

		try {
			retryHelper.get().post(
				/* task name */                  "stripeConnectAuth:" + stripeAuthCode,
				/* task queue */                 QueueFactory.getDefaultQueue(), 
				/* task age limit in seconds */  10 * 60, 
				/* millis to leave in reserve */ 30 * 1000, 				
				/* task to post */               new StripeConnectAccountDbPut(gitkitUser, stripeConnectAccount.get(), userService));
			
		} catch (Exception e) {
			// TODO: better way to handle these generic exceptions
			// TODO: need to catch and handle stripe specific exceptions
			// TODO: need to catch and handle task queue specific exceptions
			// TODO: need to catch and handle datastore specific exceptions
			resp.getWriter().println("stripe token req response: EXCEPTION");
			e.printStackTrace(resp.getWriter());
		}
	}
	
	// WARNING: DO NOT MOVE THIS CLASS, SERIALIZATION FOR TASK QUEUE WILL BE BROKEN
	public static class StripeConnectAccountDbPut implements DeferredTask, Serializable {
		private final GitkitUser gitkitUser;
		private final StripeConnectAuthorizationResponse stripeConnectRsp;
		private UserService userService;
		
		public StripeConnectAccountDbPut(
			final GitkitUser gitkitUser,
			final StripeConnectAuthorizationResponse stripeConnectAccount,
			final UserService userService
		) {
			this.gitkitUser = gitkitUser;
			this.stripeConnectRsp = stripeConnectAccount;
			this.userService = userService;
		}

		@Override
		public void run() {
			final Transaction t = 
				Datastore.beginTransaction();
			
			try {
				final User user = 
					userService.getCurrentUser(gitkitUser, t);
				
				user.setStripeUserId(stripeConnectRsp.stripe_user_id);
				user.setStripePublishableKey(stripeConnectRsp.stripe_publishable_key);
				user.setStripeAccessToken(stripeConnectRsp.access_token);
				user.setStripeRefreshToken(stripeConnectRsp.refresh_token);
				user.setStripeConnectStatus(StripeConnectStatus.CONNECTED);
				
				Datastore.put(t, user);
				
				t.commit();
				
			} catch (final NotLoggedInException e) {
				// nothing to do
				return;
				
			} finally {
			    if (t.isActive()) {
			        t.rollback();
			    }
			}
		}		
	}
}
