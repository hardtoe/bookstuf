package com.bookstuf.web;

import java.util.ConcurrentModificationException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.appengine.api.ThreadManager;
import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class CachedDatastore {
	private final DatastoreService datastore =
		DatastoreServiceFactory.getDatastoreService();

	private final ListeningExecutorService service = 
		MoreExecutors.listeningDecorator(Executors.newCachedThreadPool(ThreadManager.currentRequestThreadFactory()));
	
	public static abstract class AtomicOperation<V> {
		public abstract V execute();
	}
	
	/**
	 * Execute an atomic operation asynchronously.
	 */
	public <V> ListenableFuture<V> execute(final AtomicOperation<V> o) {
		return service.submit(new Callable<V>() {

			@Override
			public V call() {
				V returnValue = null;
				int retries = 3;
				long retryTime = (long) (50 + (Math.random() * 100.0));
				
				while (true) {
				    final Transaction txn = 
				    	datastore.beginTransaction();
				    
				    try {
				    	returnValue = o.execute();
				    	
				    	txn.commit();
				        break;
				        
				    } catch (final ConcurrentModificationException e) {
				        if (retries == 0) {
				            throw e;
				        }
				        
				        // Allow retry to occur
				        try {
							Thread.sleep(retryTime);
						} catch (final InterruptedException interruptedException) {
							// do nothing
						}
				        
				        --retries;
				        retryTime *= (1.5 + Math.random());
				        
				    } finally {
				        if (txn != null && txn.isActive()) {
				            txn.rollback();
				        }
				    }
				}
				
				return returnValue;
			}});
	}
}
