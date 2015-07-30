package com.bookstuf.datastore;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;

public class Service implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String description;
	private BigDecimal cost;
	private int durationHours;
	private int durationMinutes;
	
	private void writeObject(
		final ObjectOutputStream out
	) throws 
		IOException 
	{
		out.writeLong(serialVersionUID);
		out.writeObject(name);
		out.writeObject(description);
		out.writeObject(cost);
		out.writeInt(durationHours);
		out.writeInt(durationMinutes);
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
		durationHours = in.readInt();
		durationMinutes = in.readInt();
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
	
	public int getDurationHours() {
		return durationHours;
	}
	
	public void setDurationHours(int durationHours) {
		this.durationHours = durationHours;
	}
	
	public int getDurationMinutes() {
		return durationMinutes;
	}
	
	public void setDurationMinutes(int durationMinutes) {
		this.durationMinutes = durationMinutes;
	}
}
