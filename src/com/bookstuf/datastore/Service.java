package com.bookstuf.datastore;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;

import org.threeten.bp.Duration;

import com.bookstuf.PublicReadOnly;

public class Service implements Serializable {
	private static final long serialVersionUID = 1L;

	@PublicReadOnly
	int version = 1;
	
	@PublicReadOnly
	String id;
	
	String name;
	String description;
	BigDecimal cost;
	Duration duration;
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cost == null) ? 0 : cost.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result
				+ ((duration == null) ? 0 : duration.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		if (!(obj instanceof Service))
			return false;
		Service other = (Service) obj;
		if (cost == null) {
			if (other.cost != null)
				return false;
		} else if (!cost.equals(other.cost))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (duration == null) {
			if (other.duration != null)
				return false;
		} else if (!duration.equals(other.duration))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	private void writeObject(
		final ObjectOutputStream out
	) throws 
		IOException 
	{
		out.writeInt(version);
		out.writeObject(id);
		out.writeObject(name);
		out.writeObject(description);
		out.writeObject(cost);
		out.writeObject(duration);
	}
	
	private void readObject(
		final ObjectInputStream in
	) throws 
		IOException, 
		ClassNotFoundException 
	{
		version = in.readInt();
		id = (String) in.readObject();
		name = (String) in.readObject();
		description = (String) in.readObject();
		cost = (BigDecimal) in.readObject();
		duration = (Duration) in.readObject();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public BigDecimal getCost() {
		return cost;
	}
	
	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}
	
	public Duration getDuration() {
		return duration;
	}
	
	public void setDuration(Duration duration) {
		this.duration = duration;
	}
}
