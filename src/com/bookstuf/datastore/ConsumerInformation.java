package com.bookstuf.datastore;

import com.bookstuf.PublicReadOnly;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.OnSave;

@Cache @Entity
public class ConsumerInformation {
    @PublicReadOnly
    @Id String gitkitUserId;

    @Index String fullName;
    
    String firstName;
    String middleName;
    String lastName;
    String phoneNumber;
    String contactEmail;

    @PublicReadOnly
    boolean hasStripeCustomer;
    
    @PublicReadOnly
	String stripeCustomerId;
    
    @OnSave void onSave() {
    	fullName = lastName + ", " + firstName + " " + middleName;
    }
    
	public String getGitkitUserId() {
		return gitkitUserId;
	}
	
	public void setGitkitUserId(String gitkitUserId) {
		this.gitkitUserId = gitkitUserId;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getMiddleName() {
		return middleName;
	}
	
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public String getContactEmail() {
		return contactEmail;
	}
	
	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public void setStripeCustomerId(String stripeCustomerId) {
		this.stripeCustomerId = stripeCustomerId;
		this.hasStripeCustomer = true;
	}
	
	public String getStripeCustomerId() {
		return stripeCustomerId;
	}
	
	public boolean hasStripeCustomer() {
		return hasStripeCustomer;
	}
}
