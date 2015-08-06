package com.bookstuf.datastore;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;

import org.threeten.bp.Duration;

public class Service implements Serializable {
	private static final long serialVersionUID = 1L;
	
	String name;
	String description;
	BigDecimal cost;
	Duration duration;
	
	private void writeObject(
		final ObjectOutputStream out
	) throws 
		IOException 
	{
		out.writeLong(serialVersionUID);
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
		in.readLong(); // version
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
