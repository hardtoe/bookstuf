package com.bookstuf.datastore;

import org.slim3.datastore.Attribute;
import org.slim3.datastore.Model;

import com.bookstuf.PublicReadOnly;
import com.google.appengine.api.datastore.Key;

@Model
public class UserServices {
    @Attribute(primaryKey = true)
    @PublicReadOnly
    private Key key;

    private String aboutServices;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipcode;
    private ChargePolicy chargePolicy;
    private CancellationPolicy cancellationPolicy;
	private int cancellationDeadline;

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
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
}