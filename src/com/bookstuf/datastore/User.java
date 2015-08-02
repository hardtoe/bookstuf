package com.bookstuf.datastore;

import com.bookstuf.PublicReadOnly;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Cache @Entity
public class User {  
    @PublicReadOnly
    @Id String gitkitUserId;
    
    @PublicReadOnly
    String gitkitUserEmail;

    @PublicReadOnly
    StripeConnectStatus stripeConnectStatus;
	String stripeUserId;
    String stripePublishableKey;
    String stripeAccessToken;
    String stripeRefreshToken;
    
    @PublicReadOnly
    ProviderInformationStatus providerInformationStatus;
    
    @PublicReadOnly
    ProviderInformationStatus providerServicesStatus;

	public String getGitkitUserId() {
		return gitkitUserId;
	}

	public void setGitkitUserId(String gitkitUserId) {
		this.gitkitUserId = gitkitUserId;
	}
    
    public StripeConnectStatus getStripeConnectStatus() {
    	return stripeConnectStatus;
    }
    
    public void setStripeConnectStatus(final StripeConnectStatus stripeConnectStatus) {
    	this.stripeConnectStatus = stripeConnectStatus;
    }

	public String getStripeUserId() {
		return stripeUserId;
	}

	public void setStripeUserId(String stripeUserId) {
		this.stripeUserId = stripeUserId;
	}

	public String getStripePublishableKey() {
		return stripePublishableKey;
	}

	public void setStripePublishableKey(String stripePublishableKey) {
		this.stripePublishableKey = stripePublishableKey;
	}

	public String getStripeAccessToken() {
		return stripeAccessToken;
	}

	public void setStripeAccessToken(String stripeAccessToken) {
		this.stripeAccessToken = stripeAccessToken;
	}

	public String getStripeRefreshToken() {
		return stripeRefreshToken;
	}

	public void setStripeRefreshToken(String stripeRefreshToken) {
		this.stripeRefreshToken = stripeRefreshToken;
	}

	public String getGitkitUserEmail() {
		return gitkitUserEmail;
	}

	public void setGitkitUserEmail(String gitkitUserEmail) {
		this.gitkitUserEmail = gitkitUserEmail;
	}

	public ProviderInformationStatus getProviderInformationStatus() {
		return providerInformationStatus;
	}

	public void setProviderInformationStatus(ProviderInformationStatus providerInformationStatus) {
		this.providerInformationStatus = providerInformationStatus;
	}

	public ProviderInformationStatus getProviderServicesStatus() {
		return providerServicesStatus;
	}

	public void setProviderServicesStatus(ProviderInformationStatus providerServicesStatus) {
		this.providerServicesStatus = providerServicesStatus;
	}
}
