package com.bookstuf.web;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bookstuf.appengine.RetryHelper;
import com.google.apphosting.api.ApiProxy;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;

public abstract class RpcServlet extends HttpServlet {
	@Inject private Injector injector;
	@Inject private Provider<RetryHelper> retryHelper;
	@Inject private Gson gson;
	
	private final HashMap<String, Method> methods;
	private final HashMap<String, Publish> methodConfigurations;
	private final HashMap<Class<? extends Throwable>, Method> exceptionHandlers;
	private Method defaultMethod;

	private final ThreadLocal<Throwable> currentException;
	
	public RpcServlet(
	) {
		this.methods =
			new HashMap<>();
		
		this.methodConfigurations =
			new HashMap<>();
		
		this.exceptionHandlers =
			new HashMap<>();
		
		for (final Method m : getClass().getDeclaredMethods()) {
			if (m.isAnnotationPresent(Publish.class)) {
				methods.put(m.getName(), m);
				methodConfigurations.put(m.getName(), m.getAnnotation(Publish.class));
				m.setAccessible(true);
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
			
			if (methods.containsKey(resource)) {
				final Method m =
					methods.get(resource);
				
				final Publish methodConfiguration =
					methodConfigurations.get(resource);
				
				if (methodConfiguration.autoRetryMillis() > 0) {
					try {
					retryHelper.get().execute(
						60000 - methodConfiguration.autoRetryMillis(), 
						new Callable<Void>() {
							@Override
							public Void call() throws Exception {
								invoke(request, response, m);
								return null;
							}
						});
					} catch (final Exception e) {
						throw new RuntimeException(e);
					}
					
				} else {
					invoke(request, response, m);
				}

					
			} else {
				invoke(request, response, defaultMethod);
			}
			
		} else {
			invoke(request, response, defaultMethod);
		}
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
			
			
			if (exceptionHandlers.containsKey(causeClass)) {
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
	
	private Throwable getRealCause(final Throwable t) {
		if (
			t.getClass() == InvocationTargetException.class ||
			t.getClass() == ProvisionException.class ||
			t.getClass() == RuntimeException.class ||
			t.getClass() == ExecutionException.class
		) {
			return getRealCause(t.getCause());
			
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
}