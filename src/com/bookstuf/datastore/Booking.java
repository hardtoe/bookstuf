package com.bookstuf.datastore;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.threeten.bp.LocalTime;

import com.bookstuf.PublicReadOnly;
import com.googlecode.objectify.Key;

public class Booking implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@PublicReadOnly
	String id;
	
	Key<ProfessionalInformation> professional;
	Key<ConsumerInformation> consumer;
	Service service;
	LocalTime startTime;
	
	String location;
	String proName;
	String proEmail;
	String clientName;
	String clientEmail;
	
	PaymentMethod paymentMethod;
	String stripeCustomerId;
	String stripeChargeId;
	
	PaymentStatus paymentStatus;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((consumer == null) ? 0 : consumer.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((paymentMethod == null) ? 0 : paymentMethod.hashCode());
		result = prime * result
				+ ((professional == null) ? 0 : professional.hashCode());
		result = prime * result + ((service == null) ? 0 : service.hashCode());
		result = prime * result
				+ ((startTime == null) ? 0 : startTime.hashCode());
		result = prime * result
				+ ((stripeChargeId == null) ? 0 : stripeChargeId.hashCode());
		result = prime
				* result
				+ ((stripeCustomerId == null) ? 0 : stripeCustomerId.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Booking))
			return false;
		Booking other = (Booking) obj;
		if (consumer == null) {
			if (other.consumer != null)
				return false;
		} else if (!consumer.equals(other.consumer))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (paymentMethod != other.paymentMethod)
			return false;
		if (professional == null) {
			if (other.professional != null)
				return false;
		} else if (!professional.equals(other.professional))
			return false;
		if (service == null) {
			if (other.service != null)
				return false;
		} else if (!service.equals(other.service))
			return false;
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
			return false;
		if (stripeChargeId == null) {
			if (other.stripeChargeId != null)
				return false;
		} else if (!stripeChargeId.equals(other.stripeChargeId))
			return false;
		if (stripeCustomerId == null) {
			if (other.stripeCustomerId != null)
				return false;
		} else if (!stripeCustomerId.equals(other.stripeCustomerId))
			return false;
		return true;
	}

	private void writeObject(
		final ObjectOutputStream out
	) throws 
		IOException 
	{
		out.writeLong(2); // version
		
		out.writeObject(id);
		out.writeObject(professional);
		out.writeObject(consumer);
		out.writeObject(service);
		out.writeObject(startTime);
		
		out.writeObject(paymentMethod);
		out.writeObject(stripeCustomerId);
		out.writeObject(stripeChargeId);
		
		out.writeObject(paymentStatus);
		
		out.writeObject(location);
		out.writeObject(proName);
		out.writeObject(proEmail);
		out.writeObject(clientName);
		out.writeObject(clientEmail);
	}
	
	@SuppressWarnings("unchecked")
	private void readObject(
		final ObjectInputStream in
	) throws 
		IOException, 
		ClassNotFoundException 
	{
		final long version = in.readLong(); 

		id = (String) in.readObject();
		professional = (Key<ProfessionalInformation>) in.readObject();
		consumer = (Key<ConsumerInformation>) in.readObject();
		service = (Service) in.readObject();
		startTime = (LocalTime) in.readObject();
		
		paymentMethod = (PaymentMethod) in.readObject();
		stripeCustomerId = (String) in.readObject();
		stripeChargeId = (String) in.readObject();
		
		paymentStatus = (PaymentStatus) in.readObject();

		location = (String) in.readObject();
		proName = (String) in.readObject();
		proEmail = (String) in.readObject();
		clientName = (String) in.readObject();
		clientEmail = (String) in.readObject();
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

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentType) {
		this.paymentMethod = paymentType;
	}

	public String getStripeCustomerId() {
		return stripeCustomerId;
	}

	public void setStripeCustomerId(String stripeCustomerId) {
		this.stripeCustomerId = stripeCustomerId;
	}

	public void setStripeChargeId(final String id) {
		this.stripeChargeId = id;
	}
	
	public String getStripeChargeId() {
		return this.stripeChargeId;
	}
	
	public String getId() {
		return this.id;
	}
	
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * @return the paymentStatus
	 */
	public PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	/**
	 * @param paymentStatus the paymentStatus to set
	 */
	public void setPaymentStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Booking [\n    id=");
		builder.append(id);
		builder.append(", \nprofessional=");
		builder.append(professional);
		builder.append(", \nconsumer=");
		builder.append(consumer);
		builder.append(", \nservice=");
		builder.append(service);
		builder.append(", \nstartTime=");
		builder.append(startTime);
		builder.append(", \npaymentMethod=");
		builder.append(paymentMethod);
		builder.append(", \nstripeCustomerId=");
		builder.append(stripeCustomerId);
		builder.append(", \nstripeChargeId=");
		builder.append(stripeChargeId);
		builder.append(", \npaymentStatus=");
		builder.append(paymentStatus);
		builder.append("]");
		return builder.toString();
	}

	public String getLocation() {
		return location;
	}

	public String getProfessionalName() {
		return proName;
	}

	public String getConsumerName() {
		return clientName;
	}

	public String getProfessionalEmail() {
		return proEmail;
	}

	public String getConsumerEmail() {
		return clientEmail;
	}
	
	public void setLocation(final String location) {
		this.location = location;
	}
	
	public void setProfessionalName(final String proName) {
		this.proName = proName;
	}
	
	public void setProfessionalEmail(final String proEmail) {
		this.proEmail = proEmail;
	}
	
	public void setConsumerName(final String clientName) {
		this.clientName = clientName;
	}
	public void setConsumerEmail(final String clientEmail) {
		this.clientEmail = clientEmail;
	}
}