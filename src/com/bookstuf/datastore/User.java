package com.bookstuf.datastore;

import org.slim3.datastore.Attribute;
import org.slim3.datastore.Model;

import com.google.appengine.api.datastore.Key;

@Model
public class User {
    @Attribute(primaryKey = true)
    private Key key;
    
    private String gitkitUserId;
    private String gitkitUserEmail;
        
    private StripeConnectStatus stripeConnectStatus;
	private String stripeUserId;
    private String stripePublishableKey;
    private String stripeAccessToken;
    private String stripeRefreshToken;
    
    private ProviderInformationStatus providerInformationStatus;
    private ProviderInformationStatus providerServicesStatus;
    
	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

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
