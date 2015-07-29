package com.bookstuf.datastore;

import java.util.LinkedList;

import org.slim3.datastore.Attribute;
import org.slim3.datastore.Model;

import com.bookstuf.PublicReadOnly;
import com.google.appengine.api.datastore.Key;

@Model
public class UserInformation {
    @Attribute(primaryKey = true)
    @PublicReadOnly
    private Key key;

    // WARNING: Update status function when adding new field!
    private String handle;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String contactEmail;
    private String aboutMe;
    private LinkedList<String> photoUrls;
    
	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
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

	public LinkedList<String> getPhotoUrls() {
		return photoUrls;
	}

	public void setPhotoUrls(LinkedList<String> photoUrls) {
		this.photoUrls = photoUrls;
	}

	public ProviderInformationStatus getStatus() {
		if (
			handle != null && !handle.matches("\\s*") &&
			firstName != null && !firstName.matches("\\s*") &&
			lastName != null && !lastName.matches("\\s*") &&
			phoneNumber != null && !phoneNumber.matches("\\s*") &&
			contactEmail != null && !contactEmail.matches("\\s*") &&
			aboutMe != null && !aboutMe.matches("\\s*")
		) {
			return ProviderInformationStatus.COMPLETE;
			
		} else {
			return ProviderInformationStatus.PARTIAL;
		}
	}

}
