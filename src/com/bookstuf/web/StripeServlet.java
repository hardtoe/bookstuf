package com.bookstuf.web;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import javax.servlet.http.*;

import com.bookstuf.appengine.NotLoggedInException;
import com.bookstuf.appengine.RetryHelper;
import com.bookstuf.appengine.UserManager;
import com.bookstuf.datastore.StripeConnectStatus;
import com.bookstuf.datastore.ProfessionalPrivateInformation;
import com.bookstuf.external.StripeService;
import com.bookstuf.external.StripeService.StripeConnectAuthorizationResponse;
import com.google.identitytoolkit.GitkitClient;
import com.google.identitytoolkit.GitkitClientException;
import com.google.identitytoolkit.GitkitUser;
import com.googlecode.objectify.VoidWork;

import static com.googlecode.objectify.ObjectifyService.ofy;

@Singleton
@SuppressWarnings("serial")
public class StripeServlet extends HttpServlet {
	private final Provider<RetryHelper> retryHelper;
	private final Provider<StripeService> stripeService;
	private Provider<GitkitUser> gitkitUser;
	private UserManager userService;
	
	@Inject StripeServlet(
		final Provider<RetryHelper> retryHelper, 
		final Provider<StripeService> stripeService,
		final Provider<GitkitUser> gitkitUser,
		final UserManager userService
	) {
		this.retryHelper = retryHelper;
		this.stripeService = stripeService;
		this.gitkitUser = gitkitUser;
		this.userService = userService;
	}
	
	public void doGet(
		final HttpServletRequest req, 
		final HttpServletResponse resp
	) throws IOException {
		// TODO: handle error flow
		final String stripeAuthCode =
			req.getParameter("code");

		final Future<StripeConnectAuthorizationResponse> stripeConnectAccount =
			stripeService.get().connectAccountAsync(stripeAuthCode);
		
		resp.sendRedirect("https://www.bookstuf.com/checklist.html");

		try {
			final StripeConnectAuthorizationResponse stripeConnectRsp = 
				stripeConnectAccount.get();
				
			retryHelper.get().execute(
				15 * 1000, // leave 15 seconds in reserve
				
				new Callable<Void>() {
					@Override
					public Void call() {
						ofy().transactNew(0, new VoidWork() {
							@Override
							public void vrun() {
								final ProfessionalPrivateInformation user = 
									userService.getCurrentProfessionalPrivateInformation();
								
								user.setStripeUserId(stripeConnectRsp.stripe_user_id);
								user.setStripePublishableKey(stripeConnectRsp.stripe_publishable_key);
								user.setStripeAccessToken(stripeConnectRsp.access_token);
								user.setStripeRefreshToken(stripeConnectRsp.refresh_token);
								user.setStripeConnectStatus(StripeConnectStatus.CONNECTED);
								
								ofy().save().entity(user);
							}
						});

						return null;
					}		
				}
			);

			
		} catch (Exception e) {
			// TODO: better way to handle these generic exceptions
			// TODO: need to catch and handle stripe specific exceptions
			// TODO: need to catch and handle task queue specific exceptions
			// TODO: need to catch and handle datastore specific exceptions
			resp.getWriter().println("stripe token req response: EXCEPTION");
			e.printStackTrace(resp.getWriter());
		}
	}
}
