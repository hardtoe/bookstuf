package com.bookstuf.datastore;

import org.slim3.datastore.Attribute;
import org.slim3.datastore.Model;

import com.bookstuf.PublicReadOnly;
import com.google.appengine.api.datastore.Key;

@Model
public class UserInformation {
    @Attribute(primaryKey = true)
    @PublicReadOnly
    private Key key;

    @PublicReadOnly
    private String gitkitUserId;
    
    private String handle;
    
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String contactEmail;
    private String aboutMe;
    
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

	public String getHandle() {
		return handle;
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
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

	public String getAboutMe() {
		return aboutMe;
	}

	public void setAboutMe(String aboutMe) {
		this.aboutMe = aboutMe;
	}
}
