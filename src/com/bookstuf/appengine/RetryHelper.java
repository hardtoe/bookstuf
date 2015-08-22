package com.bookstuf.appengine;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.apphosting.api.ApiProxy;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.googlecode.objectify.Work;

import static com.googlecode.objectify.ObjectifyService.ofy;

@Singleton
public class RetryHelper {	
	public static enum Status {
		SUCCESS,
		DEFERRED;
	}

	private final Logger logger;
	private final Provider<ListeningExecutorService> listeningExecService;
	
	@Inject RetryHelper(
		final Logger logger,
		final Provider<ListeningExecutorService> listeningExecService
	) {
		this.logger = logger;
		this.listeningExecService = listeningExecService;
	}
	
	public ListenableFuture<Status> postAsync(
		final String taskName,
		final Queue queue,
		final long taskAgeLimitSeconds,
		final long millisToLeaveInReserve, 
		final DeferredTask task
	) {
		return listeningExecService.get().submit(new Callable<Status>() {
			@Override
			public Status call() throws Exception {
				return post(taskName, queue, taskAgeLimitSeconds, millisToLeaveInReserve, task);
			}
		});
	}
	
	public Status post(
		final String taskName,
		final Queue queue,
		final long taskAgeLimitSeconds,
		final long millisToLeaveInReserve, 
		final DeferredTask task
	) throws 
		Exception
	{
		final long initialRemainingMillies =
			ApiProxy.getCurrentEnvironment().getRemainingMillis();
		
		final long millisUntilReserve = 
			initialRemainingMillies - millisToLeaveInReserve;
		
		final long localRetryMillisToLeaveInReserve =
			(millisUntilReserve / 2) + millisToLeaveInReserve;
		
		try {
			execute(localRetryMillisToLeaveInReserve, new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					task.run();
					return null;
				}
			});
			
			return Status.SUCCESS;
			
		} catch (final Exception e) {
			final RetryOptions retryOptions = 
				RetryOptions.Builder.withTaskAgeLimitSeconds(taskAgeLimitSeconds);
			
			try {
				execute(millisToLeaveInReserve, new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						queue.add(
							TaskOptions.Builder
							.withPayload(task)
							.taskName(taskName)
							.retryOptions(retryOptions));
						
						return null;
					}
				});
				
				return Status.DEFERRED;
				
			} catch (final Exception eTaskQueue) {
				logger.log(Level.SEVERE, "Could not enqueue task that failed local retry.", eTaskQueue);
				throw e;
			}
		}
		
	}
	
	public <T> ListenableFuture<T> executeAsync(
		final long millisToLeaveInReserve,
		final Callable<T> callable
	) {
		return listeningExecService.get().submit(new Callable<T>() {
			@Override
			public T call() throws Exception {
				return execute(millisToLeaveInReserve, callable);
			}
		});
	}

	public <T> T transactNew(
		final long millisToLeaveInReserve,
		final Callable<T> callable
	) throws 
		Exception 
	{
		return execute(millisToLeaveInReserve, new Callable<T>() {
			@Override
			public T call() throws Exception {
				return ofy().transactNew(0, new Work<T>() {
					@Override
					public T run() {
						try {
							return callable.call();
							
						} catch(final Exception e) {
							throw new RuntimeException(e);
						}
					}
				});
			}
		});
	}
	
	public <T> T execute(
		final long millisToLeaveInReserve,
		final Callable<T> callable
	) throws 
		Exception 
	{
		long backoffMillis = 50;
		int attempt = 0;
		
		while (true) {
			try {
				return callable.call();
				
			} catch (final Exception e) {
				backoffMillis = (long) ((backoffMillis * 1.5) + (backoffMillis * Math.random()));

				final long remainingMillis = 
					ApiProxy.getCurrentEnvironment().getRemainingMillis();
				
				if ((remainingMillis - backoffMillis) > millisToLeaveInReserve) {
					logger.log(Level.WARNING, "Retrying failed task. attempt=" + attempt + ", backoffMillis=" + backoffMillis + ", remainingMillis=" + remainingMillis, e);
					Thread.sleep(backoffMillis);
					attempt++;
					
				} else {
					logger.log(Level.SEVERE, "Failed to complete retry before deadline. attempt=" + attempt, e);
					throw e;
				}
			}
		}
	}
}
