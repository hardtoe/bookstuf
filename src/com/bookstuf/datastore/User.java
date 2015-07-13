package com.bookstuf.datastore;

import org.slim3.datastore.Attribute;
import org.slim3.datastore.Model;

import com.google.appengine.api.datastore.Key;

@Model
public class User {
    @Attribute(primaryKey = true)
    private Key key;
    
    /**
     * Unique user ID from gitkit.  Must not be null;
     */
    private String gitkitUserId;
    
    /**
     * Stripe Connect Account information.  Null if not connected.
     */
    @Attribute(lob = true)
    private StripeConnectAccount stripeConnectAccount;

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

	public StripeConnectAccount getStripeConnectAccount() {
		return stripeConnectAccount;
	}

	public void setStripeConnectAccount(StripeConnectAccount stripeConnectAccount) {
		this.stripeConnectAccount = stripeConnectAccount;
	}
    
    
}
