package com.bookstuf.datastore;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.threeten.bp.DayOfWeek;

import com.bookstuf.PublicReadOnly;

public class Availability implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@PublicReadOnly
	int version = 1;
	
	DayOfWeek dayOfTheWeek;
	int startHour;
	int startMinute;
	int endHour;
	int endMinute;
	
	private void writeObject(
		final ObjectOutputStream out
	) throws 
		IOException 
	{
		out.writeInt(version);
		out.writeObject(dayOfTheWeek);
		out.writeInt(startHour);
		out.writeInt(startMinute);
		out.writeInt(endHour);
		out.writeInt(endMinute);
	}
	
	private void readObject(
		final ObjectInputStream in
	) throws 
		IOException, 
		ClassNotFoundException 
	{
		version = in.readInt();
		dayOfTheWeek = (DayOfWeek) in.readObject();
		startHour = in.readInt();
		startMinute = in.readInt();
		endHour = in.readInt();
		endMinute = in.readInt();
	}

	public DayOfWeek getDayOfTheWeek() {
		return dayOfTheWeek;
	}

	public void setDayOfTheWeek(DayOfWeek dayOfTheWeek) {
		this.dayOfTheWeek = dayOfTheWeek;
	}

	public int getStartHour() {
		return startHour;
	}

	public void setStartHour(int startHour) {
		this.startHour = startHour;
	}

	public int getStartMinute() {
		return startMinute;
	}

	public void setStartMinute(int startMinute) {
		this.startMinute = startMinute;
	}

	public int getEndHour() {
		return endHour;
	}

	public void setEndHour(int endHour) {
		this.endHour = endHour;
	}

	public int getEndMinute() {
		return endMinute;
	}

	public void setEndMinute(int endMinute) {
		this.endMinute = endMinute;
	}
}
