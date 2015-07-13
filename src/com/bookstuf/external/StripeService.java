package com.bookstuf.external;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.bookstuf.datastore.StripeConnectAccount;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.gson.Gson;

@RequestScoped
public class StripeService {
	private final Logger logger;
	private final URLFetchService urlFetchService;
	private final ListeningExecutorService listeningExecService;
	
	private final Gson gson;
	
	@Inject StripeService(
		final Logger logger,
		final URLFetchService urlFetchService,
		final ListeningExecutorService listeningExecService
	) {
		this.logger = logger;
		this.urlFetchService = urlFetchService;
		this.listeningExecService = listeningExecService;
		
		this.gson = new Gson();
	}
	
	public ListenableFuture<StripeConnectAccount> connectAccountAsync(
		final String authorizationCode
	) {
		return listeningExecService.submit(new Callable<StripeConnectAccount>() {
			@Override
			public StripeConnectAccount call() throws Exception {
				return connectAccount(authorizationCode);
			}
		});
	}
	
	public StripeConnectAccount connectAccount(
		final String authorizationCode
	) throws 
		IOException 
	{	
		HTTPRequest stripeTokenRequest;
		try {
			stripeTokenRequest = 
				new HTTPRequest(new URL("https://connect.stripe.com/oauth/token"), HTTPMethod.POST);
			
		} catch (final MalformedURLException e) {
			logger.log(Level.SEVERE, "Received malformed url exception for a hard-coded URL", e);
			throw new RuntimeException(e);
		}
		
		stripeTokenRequest.setPayload(
			("client_id=ca_6alXrQKPz1whryd22S7nu6widUhQasz7&" +
			"client_secret=sk_test_NkEKdstqaaibV1iTBeH73mGC&" +
			"code=" + authorizationCode + "&" +
			"grant_type=authorization_code").getBytes());
		
		final HTTPResponse stripeTokenResponse = 
			urlFetchService.fetch(stripeTokenRequest);
		
		final String response =
			new String(stripeTokenResponse.getContent());
		
		final StripeConnectAuthorizationResponse rsp =
			gson.fromJson(response, StripeConnectAuthorizationResponse.class);
		
		// TODO: handle stripe error
		
		final StripeConnectAccount stripeConnectAccount =
			new StripeConnectAccount(rsp.stripe_user_id, rsp.stripe_publishable_key, rsp.access_token, rsp.refresh_token);
		
		return stripeConnectAccount;
	}

	private static class StripeConnectAuthorizationResponse {
		public String access_token;
		public String refresh_token;
		public String token_type;
		public String stripe_publishable_key;
		public String stripe_user_id;
		public String scope;
		
		public String error;
		public String error_description;
	}
}
