package com.bookstuf.datastore;

import com.bookstuf.PublicReadOnly;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Cache @Entity
public class ConsumerInformation {
    @PublicReadOnly
    @Id String gitkitUserId;

    String firstName;
    String middleName;
    String lastName;
    String phoneNumber;
    String contactEmail;
    
    String addressLine1;
    String addressLine2;
    String city;
    String state;
    String zipcode;
    
    
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
	
	public String getAddressLine1() {
		return addressLine1;
	}
	
	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}
	
	public String getAddressLine2() {
		return addressLine2;
	}
	
	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}
	
	public String getCity() {
		return city;
	}
	
	public void setCity(String city) {
		this.city = city;
	}
	
	public String getState() {
		return state;
	}
	
	public void setState(String state) {
		this.state = state;
	}
	
	public String getZipcode() {
		return zipcode;
	}
	
	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}
}
