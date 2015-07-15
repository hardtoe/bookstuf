package com.bookstuf.appengine;

public interface KeyStore {
	public String getStripeClientId();
	public String getStripeClientSecret();
	
	public String getGoogleClientId();
	public String getGoogleServiceAccountEmail();
}
