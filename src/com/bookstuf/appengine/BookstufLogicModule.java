package com.bookstuf.appengine;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.inject.name.Named;
import com.google.inject.Singleton;

import com.google.appengine.api.ThreadManager;
import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.taskqueue.TransientFailureException;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.servlet.RequestScoped;

public class BookstufLogicModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(URLFetchService.class).toInstance(URLFetchServiceFactory.getURLFetchService());
	}

	@Provides @RequestScoped public ExecutorService getThreadPool() {
		return Executors.newCachedThreadPool(ThreadManager.currentRequestThreadFactory());
	}

	@Provides @RequestScoped public ListeningExecutorService getListeningExecService(final ExecutorService threadPool) {
		return MoreExecutors.listeningDecorator(threadPool);
	}
	
	@Provides @Singleton @Named("retriable-exceptions") public HashSet<Class<?>> getRetriableExceptions() {
		final HashSet<Class<?>> retriableExceptions =
			new HashSet<>();
		
		retriableExceptions.add(TransientFailureException.class);
		retriableExceptions.add(DatastoreTimeoutException.class);
		
		return retriableExceptions;
	}
}
