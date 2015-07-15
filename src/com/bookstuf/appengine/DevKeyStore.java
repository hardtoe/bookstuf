package com.bookstuf.appengine;

import com.google.inject.Singleton;

@Singleton
public class DevKeyStore implements KeyStore {

	@Override
	public String getStripeClientId() {
		return "ca_6alXrQKPz1whryd22S7nu6widUhQasz7";
	}

	@Override
	public String getStripeClientSecret() {
		return "sk_test_NkEKdstqaaibV1iTBeH73mGC";
	}

	@Override
	public String getGoogleClientId() {
		return "1022706286728-22r7bucp7mp3kk7vdhk7kvda5pohkgcg.apps.googleusercontent.com";
	}

	@Override
	public String getGoogleServiceAccountEmail() {
		return "1022706286728-hebtee767c8jen3odtbcvlle7ok6rh7k@developer.gserviceaccount.com";
	}

}
