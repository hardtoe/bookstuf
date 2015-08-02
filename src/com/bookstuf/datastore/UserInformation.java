package com.bookstuf.datastore;

import java.util.Iterator;
import java.util.LinkedList;

import org.slim3.datastore.Attribute;
import org.slim3.datastore.Model;

import com.bookstuf.PublicReadOnly;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Key;

@Model
public class UserInformation {
    @Attribute(primaryKey = true)
    @PublicReadOnly
    private Key key;

    // WARNING: Update status function when adding new field!
    
    // INFORMATION
    private String handle;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String contactEmail;
    private String aboutMe;
    
    @Attribute(unindexed = true)
    private LinkedList<String> photoUrls;

    // SERVICES
    private String aboutServices;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipcode;
    private ChargePolicy chargePolicy;
    private CancellationPolicy cancellationPolicy;
	private int cancellationDeadline;
	
	@Attribute(lob = true)
	private LinkedList<Service> services;
	
	@Attribute(lob = true)
	private LinkedList<Availability> availability;

	
    public void addPhoto(final BlobKey blobKey, final String url) {
    	photoUrls.add(blobKey.getKeyString() + " " + url);
    }
    
    public BlobKey removePhoto(final String url) {
    	final Iterator<String> i =
    		photoUrls.iterator();
    	
    	while (i.hasNext()) {
    		final String entry = i.next();
    		final String[] fields = entry.split("\\s+");
    		final String blobKey = fields[0];
    		final String photoUrl = fields[1];
    		
    		if (photoUrl.equals(url)) {
    			i.remove();
    			return new BlobKey(blobKey);
    		}
    	}
    	
    	return null;
    }
    
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

	public ProviderInformationStatus getInformationStatus() {
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



	public String getAboutServices() {
		return aboutServices;
	}

	public void setAboutServices(String aboutServices) {
		this.aboutServices = aboutServices;
	}

	public ChargePolicy getChargePolicy() {
		return chargePolicy;
	}

	public void setChargePolicy(ChargePolicy chargePolicy) {
		this.chargePolicy = chargePolicy;
	}

	public CancellationPolicy getCancellationPolicy() {
		return cancellationPolicy;
	}

	public void setCancellationPolicy(CancellationPolicy cancellationPolicy) {
		this.cancellationPolicy = cancellationPolicy;
	}

	public int getCancellationDeadline() {
		return cancellationDeadline;
	}

	public void setCancellationDeadline(int cancellationDeadline) {
		this.cancellationDeadline = cancellationDeadline;
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
	
	public LinkedList<Service> getServices() {
		return services;
	}

	public void setServices(LinkedList<Service> services) {
		this.services = services;
	}

	public LinkedList<Availability> getAvailability() {
		return availability;
	}

	public void setAvailability(LinkedList<Availability> availability) {
		this.availability = availability;
	}

	public ProviderInformationStatus getServicesStatus() {
		if (
			aboutServices != null && !aboutServices.matches("\\s*") &&
			addressLine1 != null && !addressLine1.matches("\\s*") &&
			city != null && !city.matches("\\s*") &&
			state != null && !state.matches("\\s*") &&
			zipcode != null && !zipcode.matches("\\s*") &&
			services.size() > 0 && 
			availability.size() > 0
		) {
			return ProviderInformationStatus.COMPLETE;
			
		} else {
			return ProviderInformationStatus.PARTIAL;
		}
	}
}
