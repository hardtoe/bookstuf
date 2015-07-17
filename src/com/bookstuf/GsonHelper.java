package com.bookstuf;

import java.io.Reader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GsonHelper {
	private final Logger logger;
	private final Gson gson;
	private final JsonParser jsonParser;
	
	@Inject GsonHelper(
		final Logger logger,
		final Gson gson
	) {
		this.logger = logger;
		this.gson = gson;
		
		this.jsonParser = new JsonParser();
	}
	
	public <T> T updateFromJson(
		final Reader reader, 
		final T instance
	) {
		return updateFromJson(jsonParser.parse(reader), instance);
	}
	
	public <T> T updateFromJson(
		final JsonElement jsonElement, 
		final T instance
	) {
		final JsonObject jsonObject = 
			jsonElement.getAsJsonObject();
		
		final ArrayList<Field> instanceFields =
			getAllFields(new ArrayList<Field>(), instance.getClass());
		
		for (final Field f : instanceFields) {
			try {
				final Object existingValue =
					f.get(instance);
				
				if (jsonObject.has(f.getName())) {
					// only update fields we have
					
					final JsonElement fieldElement =
						jsonObject.get(f.getName());
					
					if (existingValue == null) {
						// replace null fields
						f.set(instance, gson.fromJson(fieldElement, f.getGenericType()));
						
					} else {
						// merge existing fields
						updateFromJson(fieldElement, existingValue);
					}
				}
			} catch (IllegalAccessException e) {
				logger.log(Level.SEVERE, "Unable to update field with JSON", e);
			}
		}

		return instance;
	}
	
	
	
	
	
	

	
	
	
	
	
	
	
	/*
	 * Adapted from: http://stackoverflow.com/questions/12518618/deserialize-json-into-existing-object-java
	 */
	private ArrayList<Field> getAllFields(
		final ArrayList<Field> fields, 
		final Class<?> type
	) {
	    for (Field field : type.getDeclaredFields()) {
	        fields.add(field);
	    }
	    
	    if (type.getSuperclass() != null && type.getSuperclass() != Object.class) {
	        getAllFields(fields, type.getSuperclass());
	    }
	    
	    return fields;
	}
}
