package com.bookstuf;

import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public abstract class Memcacheable<Key, Value> {
	private final MemcacheService memcache = 
		MemcacheServiceFactory.getMemcacheService();
	
	private final AsyncMemcacheService asyncMemcache =
		MemcacheServiceFactory.getAsyncMemcacheService();
	
	public final void updateCache(final Key key, final Value value) {
		asyncMemcache.put(key, value);
	}
	
	public final Value get(final Key key) {
		@SuppressWarnings("unchecked")
		Value value = 
			(Value) memcache.get(key);
		
		if (value == null) {
			value = generate(key);
			updateCache(key, value);
		}
		
		return value;
	}
	
	protected abstract Value generate(final Key key);
}
