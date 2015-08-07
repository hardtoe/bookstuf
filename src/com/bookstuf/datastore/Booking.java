package com.bookstuf.datastore;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.threeten.bp.LocalTime;

import com.googlecode.objectify.Key;

public class Booking implements Serializable {
	private static final long serialVersionUID = 1L;
	
	Key<ProfessionalInformation> professional;
	Key<ConsumerInformation> consumer;
	Service service;
	LocalTime startTime;
	
	PaymentMethod paymentType;
	String stripeCustomerId;
	String stripeCardId;
	
	PaymentStatus paymentStatus;
	
	private void writeObject(
		final ObjectOutputStream out
	) throws 
		IOException 
	{
		out.writeLong(serialVersionUID);
		out.writeObject(professional);
		out.writeObject(consumer);
		out.writeObject(service);
		out.writeObject(startTime);
		
		out.writeObject(paymentType);
		out.writeObject(stripeCustomerId);
		out.writeObject(stripeCardId);
		
		out.writeObject(paymentStatus);
	}
	
	@SuppressWarnings("unchecked")
	private void readObject(
		final ObjectInputStream in
	) throws 
		IOException, 
		ClassNotFoundException 
	{
		in.readLong(); // version
		professional = (Key<ProfessionalInformation>) in.readObject();
		consumer = (Key<ConsumerInformation>) in.readObject();
		service = (Service) in.readObject();
		startTime = (LocalTime) in.readObject();
		
		paymentType = (PaymentMethod) in.readObject();
		stripeCustomerId = (String) in.readObject();
		stripeCardId = (String) in.readObject();
		
		paymentStatus = (PaymentStatus) in.readObject();
	}

	public Key<ProfessionalInformation> getProfessional() {
		return professional;
	}

	public void setProfessional(Key<ProfessionalInformation> professional) {
		this.professional = professional;
	}

	public Key<ConsumerInformation> getConsumer() {
		return consumer;
	}

	public void setConsumer(Key<ConsumerInformation> consumer) {
		this.consumer = consumer;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public PaymentMethod getPaymentType() {
		return paymentType;
	}

	public void setPaymentMethod(PaymentMethod paymentType) {
		this.paymentType = paymentType;
	}

	public String getStripeCustomerId() {
		return stripeCustomerId;
	}

	public void setStripeCustomerId(String stripeCustomerId) {
		this.stripeCustomerId = stripeCustomerId;
	}

	public String getStripeCardId() {
		return stripeCardId;
	}

	public void setStripeCardId(String stripeCardId) {
		this.stripeCardId = stripeCardId;
	}

	public PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}
}