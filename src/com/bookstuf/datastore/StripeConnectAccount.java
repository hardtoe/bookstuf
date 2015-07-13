package com.bookstuf.datastore;

import java.io.Serializable;

public class StripeConnectAccount implements Serializable {
	private static final long serialVersionUID = 1938158880356318527L;

	private String userId;
    private String publishableKey;
    private String accessToken;
    private String refreshToken;
    
    public StripeConnectAccount(
    	final String userId,
    	final String publishableKey,
    	final String accessToken,
    	final String refreshToken
    ) {
    	this.userId = userId;
    	this.publishableKey = publishableKey;
    	this.accessToken = accessToken;
    	this.refreshToken = refreshToken;
    }

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getUserId() {
		return userId;
	}

	public String getPublishableKey() {
		return publishableKey;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}
}
