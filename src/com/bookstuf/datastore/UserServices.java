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
    
    @PublicReadOnly
    private String gitkitUserId;

    private String aboutServices;
    private String address;
    private ChargePolicy chargePolicy;
    private CancellationPolicy cancellationPolicy;
	private int cancellationDeadline;

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

	public String getAboutServices() {
		return aboutServices;
	}

	public void setAboutServices(String aboutServices) {
		this.aboutServices = aboutServices;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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
}
