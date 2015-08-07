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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((consumer == null) ? 0 : consumer.hashCode());
		result = prime * result
				+ ((paymentStatus == null) ? 0 : paymentStatus.hashCode());
		result = prime * result
				+ ((paymentType == null) ? 0 : paymentType.hashCode());
		result = prime * result
				+ ((professional == null) ? 0 : professional.hashCode());
		result = prime * result + ((service == null) ? 0 : service.hashCode());
		result = prime * result
				+ ((startTime == null) ? 0 : startTime.hashCode());
		result = prime * result
				+ ((stripeCardId == null) ? 0 : stripeCardId.hashCode());
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
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Booking)) {
			return false;
		}
		Booking other = (Booking) obj;
		if (consumer == null) {
			if (other.consumer != null) {
				return false;
			}
		} else if (!consumer.equals(other.consumer)) {
			return false;
		}
		if (paymentStatus != other.paymentStatus) {
			return false;
		}
		if (paymentType != other.paymentType) {
			return false;
		}
		if (professional == null) {
			if (other.professional != null) {
				return false;
			}
		} else if (!professional.equals(other.professional)) {
			return false;
		}
		if (service == null) {
			if (other.service != null) {
				return false;
			}
		} else if (!service.equals(other.service)) {
			return false;
		}
		if (startTime == null) {
			if (other.startTime != null) {
				return false;
			}
		} else if (!startTime.equals(other.startTime)) {
			return false;
		}
		if (stripeCardId == null) {
			if (other.stripeCardId != null) {
				return false;
			}
		} else if (!stripeCardId.equals(other.stripeCardId)) {
			return false;
		}
		if (stripeCustomerId == null) {
			if (other.stripeCustomerId != null) {
				return false;
			}
		} else if (!stripeCustomerId.equals(other.stripeCustomerId)) {
			return false;
		}
		return true;
	}

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