package com.bookstuf.web.billing;

import java.util.logging.Logger;

import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Work;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class SchemaUpdateMapper extends MapOnlyMapper<com.google.appengine.api.datastore.Key, Void> {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(SchemaUpdateMapper.class.getName());
	
	private Class<?> entityType;
	  
	public SchemaUpdateMapper(final Class<?> entityType) {
		this.entityType = entityType;
	}

	@Override
	public void map(
		final com.google.appengine.api.datastore.Key rawKey
	) {
		ofy().transactNew(5, new Work<Void>() {
			@Override
			public Void run() {
				final Key<?> key =
					Key.create(rawKey);
				
				log.info("updating schema for " + key);
				
				final Object entity = 
					ofy().load().key(key).now();
				
				log.info("type of entity: " + entity.getClass().getSimpleName());
				
				ofy().save().entity(entity);
				
				return null;
			}});
	}
}
