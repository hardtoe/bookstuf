package com.bookstuf;

import java.util.Iterator;

import org.slim3.datastore.Datastore;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query.FilterOperator;

public class DatastoreHelper {
	public static <T> T getInstanceByProperty(
		final Transaction txn,
		final Class<T> type,
		final String property,
		final Object value
	) {
		final Iterator<Key> resultIterator = 
			Datastore
				.query(type)
				.filter(
					property, 
					FilterOperator.EQUAL, 
					value)
				.asKeyIterator();
		
		T result;
		
		if (resultIterator.hasNext()) {
			if (txn == null) {
				result = Datastore.get(type, resultIterator.next());
				
			} else {
				result = Datastore.get(txn, type, resultIterator.next());
			}
			
		} else {
			result = null;
		}
		
		return result;
	}
}
