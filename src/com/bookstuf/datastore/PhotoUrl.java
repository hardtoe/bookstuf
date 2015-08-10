package com.bookstuf.datastore;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.bookstuf.PublicReadOnly;

public class PhotoUrl implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@PublicReadOnly
	int version = 1;
	
	String blobKey;
	String url;
	
	public PhotoUrl(final String blobKey, final String url) {
		this.blobKey = blobKey;
		this.url = url;
	}
	
	public PhotoUrl() {
		// do nothing
	}

	private void writeObject(
		final ObjectOutputStream out
	) throws 
		IOException 
	{
		out.writeInt(version);
		out.writeObject(blobKey);
		out.writeObject(url);
	}
	
	private void readObject(
		final ObjectInputStream in
	) throws 
		IOException, 
		ClassNotFoundException 
	{
		version = in.readInt(); // version
		blobKey = (String) in.readObject();
		url = (String) in.readObject();
	}
	
	public String getBlobKey() {
		return blobKey;
	}
	
	public void setBlobKey(final String blobKey) {
		this.blobKey = blobKey;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(final String url) {
		this.url = url;
	}
}
