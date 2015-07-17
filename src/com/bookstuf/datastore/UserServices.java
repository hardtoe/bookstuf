package com.bookstuf.datastore;

import org.slim3.datastore.Attribute;
import org.slim3.datastore.Model;

import com.google.appengine.api.datastore.Key;

@Model
public class UserServices {
    @Attribute(primaryKey = true)
    private Key key;
    
    private String gitkitUserId;

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
}
