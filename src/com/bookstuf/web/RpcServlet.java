package com.bookstuf.web;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bookstuf.appengine.RetryHelper;
import com.bookstuf.web.booking.RequestError;
import com.google.appengine.api.log.LogService.LogLevel;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.googlecode.objectify.VoidWork;

public abstract class RpcServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Inject private Injector injector;
	@Inject private RetryHelper retryHelper;
	@Inject protected Gson gson;
	@Inject protected Logger logger;
	
	private final HashMap<String, Handler> handlers;
	private final HashMap<Class<? extends Throwable>, Method> exceptionHandlers;
	private Method defaultMethod;

	private final ThreadLocal<Throwable> currentException;
	
	public RpcServlet() {
		this.handlers =
			new HashMap<>();
		
		this.exceptionHandlers =
			new HashMap<>();
		
		for (final Method m : getClass().getDeclaredMethods()) {
			if (m.isAnnotationPresent(Publish.class)) {
				handlers.put(m.getName(), wrapInHandler(m));
			}
			
			if (m.isAnnotationPresent(Default.class)) {
				defaultMethod = m;
				m.setAccessible(true);
			}
			
			if (m.isAnnotationPresent(ExceptionHandler.class)) {
				final ExceptionHandler e =
					m.getAnnotation(ExceptionHandler.class);
				
				exceptionHandlers.put(e.value(), m);
				
				m.setAccessible(true);
			}
		}
		
		this.currentException = new ThreadLocal<Throwable>();
	}
	
	private Handler wrapInHandler(final Method m) {
		m.setAccessible(true);
		
		final Publish methodConfiguration =
			m.getAnnotation(Publish.class);
		
		Handler handler =
			new Handler() {
				@Override public void run(
					final HttpServletRequest request,
					final HttpServletResponse response
				) {	
					invoke(request, response, m);
				}
			};
		
		if (m.isAnnotationPresent(AsTransaction.class)) {
			handler = wrapInTransaction(handler);
		}
		
		if (methodConfiguration.withAutoRetryMillis() > 0) {
			handler = wrapInAutoRetry(methodConfiguration.withAutoRetryMillis(), handler);
		}
		
		return handler;
	}

	@Override
	protected final void doGet(
		final HttpServletRequest request,
		final HttpServletResponse response
	) throws
		ServletException, 
		IOException 
	{
		handleRequest(request, response);
	}
	
	@Override
	protected final void doPost(
		final HttpServletRequest request,
		final HttpServletResponse response
	) throws
		ServletException, 
		IOException 
	{
		handleRequest(request, response);
	}
	
	private void handleRequest(
		final HttpServletRequest request,
		final HttpServletResponse response
	) throws
		ServletException, 
		IOException 
	{
		final String[] path =
			request.getPathInfo() == null ? 
				null : 
				request.getPathInfo().split("/");
		
		if (path != null && path.length > 0) {
			final String resource =
				path[1];
			
			if (handlers.containsKey(resource)) {
				handlers.get(resource).run(request, response);
			
			} else {
				invoke(request, response, defaultMethod);
			}
			
		} else {
			invoke(request, response, defaultMethod);
		}
	}

	private Handler wrapInAutoRetry(
		final long autoRetryMillis, 
		final Handler runnable
	) {
		return 
			new Handler() {
				@Override public void run(
					final HttpServletRequest request,
					final HttpServletResponse response
				) {
					try {
						retryHelper.execute(
							60000 - autoRetryMillis, 
							new Callable<Void>() {
								@Override
								public Void call() throws Exception {
									runnable.run(request, response);
									return null;
								}
							});
						
					} catch (final Exception e) {
						throw new RuntimeException(e);
					}				
				}
			};
	}
	
	private Handler wrapInTransaction(final Handler runnable) {
		return 
			new Handler() {
				@Override public void run(
					final HttpServletRequest request,
					final HttpServletResponse response
				) {
					ofy().transactNew(0, new VoidWork() {		
						@Override
						public void vrun() {
							runnable.run(request, response);
						}
					});								
				}
			};
	}
	
	private void invoke(
		final HttpServletRequest request, 
		final HttpServletResponse response,
		final Method m
	) {		
		final Object[] args =
			injectArgs(m, request);
		
		try {
			final Object returnValue = 
				m.invoke(this, args);
			
			try {
				if (m.getReturnType() != Void.TYPE) {
					if (m.getReturnType() == String.class) {
						response.getWriter().print(returnValue);
					} else {
						gson.toJson(returnValue, m.getReturnType(), new JsonWriter(response.getWriter()));
					}
				}
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
			
		} catch (final InvocationTargetException e) {
			final Throwable realCause =
				getRealCause(e);
			
			final Class<? extends Throwable> causeClass =
				realCause.getClass();
			
			
			if (realCause instanceof RequestError) {
				try {
					logger.log(Level.WARNING, "Reporting error on request", e);
					error(response, realCause.getMessage());
					
				} catch (IOException ioException) {
					throw new RuntimeException(ioException);
				}
				
			} else if (exceptionHandlers.containsKey(causeClass)) {
				final Method exceptionHandler =
					exceptionHandlers.get(causeClass);
				
				currentException.set(realCause);
				
				invoke(request, response, exceptionHandler);
				
			} else {
				throw new RuntimeException(e);
			}
			
		} catch (final IllegalAccessException | IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void error(
		final HttpServletResponse response, 
		final String message
	) throws IOException {
		response.getWriter().print("{\"success\": false, \"error\" : \"" + message + "\"}");
		response.setStatus(400);
	}
	
	protected Throwable getRealCause(final Throwable t) {
		if (
			t.getClass() == InvocationTargetException.class ||
			t.getClass() == ProvisionException.class ||
			t.getClass() == RuntimeException.class ||
			t.getClass() == ExecutionException.class
		) {
			if (t.getCause() == null) {
				return t;
			} else {
				return getRealCause(t.getCause());
			}
			
		} else {
			return t;
		}
	}

	protected Throwable getCurrentException() {
		return currentException.get();
	}
	
	private Object[] injectArgs(
		final Method m, 
		final HttpServletRequest request
	) {
		final Object[] args = 
			new Object[m.getParameterTypes().length];

		final Annotation[][] parameterAnnotations =
			m.getParameterAnnotations();
		
		final Class<?>[] parameterTypes =
			m.getParameterTypes();
		
		for (int i = 0; i < parameterAnnotations.length; i++) {
			if (getParamAnnotation(Param.class, parameterAnnotations[i]) != null) {
				// use parameter value
				final Param paramAnnotation =
					getParamAnnotation(Param.class, parameterAnnotations[i]);
				
				args[i] =
					fromString(parameterTypes[i], request.getParameter(paramAnnotation.value()));				
			
			} else if (getParamAnnotation(RequestBody.class, parameterAnnotations[i]) != null) {
				try {
					args[i] = gson.fromJson(request.getReader(), parameterTypes[i]);
					
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
				
			} else {
			// inject via guice
			args[i] =
				injector.getInstance(parameterTypes[i]);
			}
			
		}
		
		return args;
	}

	private Object fromString(
		final Class<?> targetClass, 
		final String string
	) {
		if (targetClass == String.class) {
			return string;
			
		} else if (targetClass == boolean.class || targetClass == Boolean.class) {
			return Boolean.valueOf(string);
			
		} else if (targetClass == int.class || targetClass == Integer.class) {
			return Integer.valueOf(string);
			
		} else if (targetClass == long.class || targetClass == Long.class) {
			return Long.valueOf(string);
			
		} else if (targetClass == float.class || targetClass == Float.class) {
			return Float.valueOf(string);
			
		} else if (targetClass == double.class || targetClass == Double.class) {
			return Double.valueOf(string);
			
		} else {
			return null;
		}
	}

	private <T extends Annotation> T getParamAnnotation(
		final Class<? extends Annotation> annotationClass, 
		final Annotation[] annotations
	) {
		for (final Annotation a : annotations) {
			
			if (annotationClass.isAssignableFrom(a.getClass())) {
				return (T) a;
			}
		}
		
		return null;
	}
	
	private static abstract class Handler {
		public abstract void run(
			final HttpServletRequest request,
			final HttpServletResponse response);
		
	}
}
