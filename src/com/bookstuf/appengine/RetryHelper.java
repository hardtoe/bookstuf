package com.bookstuf.appengine;

import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.apphosting.api.ApiProxy;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.name.Named;
import com.google.inject.servlet.RequestScoped;

@RequestScoped
public class RetryHelper {	
	public static enum Status {
		SUCCESS,
		DEFERRED;
	}

	private final Logger logger;
	private final ListeningExecutorService listeningExecService;
	private final HashSet<Class<?>> retriableExceptions;
	
	@Inject RetryHelper(
		final Logger logger,
		final ListeningExecutorService listeningExecService,
		final @Named("retriable-exceptions") HashSet<Class<?>> retriableExceptions
	) {
		this.logger = logger;
		this.listeningExecService = listeningExecService;
		this.retriableExceptions = retriableExceptions;
	}
	
	private boolean isRetriableException(
		final Throwable t
	) {
		return retriableExceptions.contains(t.getClass());
	}
	
	public ListenableFuture<Status> postAsync(
		final String taskName,
		final Queue queue,
		final long taskAgeLimitSeconds,
		final long millisToLeaveInReserve, 
		final DeferredTask task
	) {
		return listeningExecService.submit(new Callable<Status>() {
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
			if (isRetriableException(e)) {
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
			} else {
				throw e;
			}
		}
		
	}
	
	public <T> ListenableFuture<T> executeAsync(
		final long millisToLeaveInReserve,
		final Callable<T> callable
	) {
		return listeningExecService.submit(new Callable<T>() {
			@Override
			public T call() throws Exception {
				return execute(millisToLeaveInReserve, callable);
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
		
		while (true) {
			try {
				return callable.call();
				
			} catch (final Exception e) {
				if (isRetriableException(e)) {
					backoffMillis = (long) ((backoffMillis * 1.5) + (backoffMillis * Math.random()));

					if ((ApiProxy.getCurrentEnvironment().getRemainingMillis() - backoffMillis) > millisToLeaveInReserve) {
						logger.log(Level.INFO, "Retrying failed task.", e);
						Thread.sleep(backoffMillis);
						
					} else {
						logger.log(Level.WARNING, "Failed to complete retry before deadline.", e);
						throw e;
					}
				} else {
					throw e;
				}
			}
		}
	}
}
