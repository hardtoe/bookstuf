package com.bookstuf.appengine;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLHandshakeException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.identitytoolkit.GitkitClient;
import com.google.identitytoolkit.GitkitClientException;
import com.google.identitytoolkit.GitkitUser;
import com.google.inject.name.Named;
import com.google.inject.Singleton;
import com.google.appengine.api.ThreadManager;
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.appengine.api.taskqueue.TransientFailureException;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.tools.mapreduce.MapReduceServlet;
import com.google.appengine.tools.mapreduce.servlets.ShufflerServlet;
import com.google.appengine.tools.pipeline.impl.servlets.PipelineServlet;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.servlet.RequestScoped;
import com.googlecode.objectify.ObjectifyFilter;

public class BookstufLogicModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(ObjectifyFilter.class).in(Singleton.class);


		bind(PipelineServlet.class).in(Singleton.class);
		bind(MapReduceServlet.class).in(Singleton.class);
		//bind(ShufflerServlet.class).in(Singleton.class);
		
		
		bind(URLFetchService.class).toInstance(URLFetchServiceFactory.getURLFetchService());
		bind(KeyStore.class).to(DevKeyStore.class);
	}
	
	@Test public void testDateParsing() {
		final DateTimeFormatter dateFormat =
			DateTimeFormatter.ofPattern("M-d-yyyy");
		
		final String date = "8-8-2015";
		
		
		System.out.println(LocalDate.parse(date, dateFormat));
	}
	
	@Provides @Singleton public Gson getGson() {
		final GsonBuilder gsonBuilder =
			new GsonBuilder();

		final DateTimeFormatter dateFormat =
			DateTimeFormatter.ofPattern("M/d/yyyy");
		
		gsonBuilder.registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
			@Override
			public JsonElement serialize(
				final LocalDate src, 
				final Type typeOfSrc,
				final JsonSerializationContext context
			) {
				return new JsonPrimitive(src.format(dateFormat));
			}
		});
		
		gsonBuilder.registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
			@Override
			public LocalDate deserialize(
				final JsonElement json, 
				final Type typeOfSrc,
				final JsonDeserializationContext context
			) throws JsonParseException {
				final String date =
					json.getAsJsonPrimitive().getAsString();
				
				return LocalDate.parse(date, dateFormat);
			}
		});

		final DateTimeFormatter timeFormat =
			DateTimeFormatter.ofPattern("h:mm a");
		
		gsonBuilder.registerTypeAdapter(LocalTime.class, new JsonSerializer<LocalTime>() {
			@Override
			public JsonElement serialize(
				final LocalTime src, 
				final Type typeOfSrc,
				final JsonSerializationContext context
			) {
				return new JsonPrimitive(src.format(timeFormat));
			}
		});
		
		gsonBuilder.registerTypeAdapter(LocalTime.class, new JsonDeserializer<LocalTime>() {
			@Override
			public LocalTime deserialize(
				final JsonElement json, 
				final Type typeOfSrc,
				final JsonDeserializationContext context
			) throws JsonParseException {
				return LocalTime.parse(json.getAsJsonPrimitive().getAsString(), timeFormat);
			}
		});
		
		gsonBuilder.registerTypeAdapter(Duration.class, new JsonSerializer<Duration>() {
			@Override
			public JsonElement serialize(
				final Duration src, 
				final Type typeOfSrc,
				final JsonSerializationContext context
			) {
				return new JsonPrimitive(src.toMinutes());
			}
		});
		
		gsonBuilder.registerTypeAdapter(Duration.class, new JsonDeserializer<Duration>() {
			@Override
			public Duration deserialize(
				final JsonElement json, 
				final Type typeOfSrc,
				final JsonDeserializationContext context
			) throws JsonParseException {
				return Duration.ofMinutes(json.getAsJsonPrimitive().getAsNumber().longValue());
			}
		});
		
		gsonBuilder.registerTypeAdapter(ZoneId.class, new JsonSerializer<ZoneId>() {
			@Override
			public JsonElement serialize(
				final ZoneId src, 
				final Type typeOfSrc,
				final JsonSerializationContext context
			) {
				return new JsonPrimitive(src.getId());
			}
		});
		
		gsonBuilder.registerTypeAdapter(ZoneId.class, new JsonDeserializer<ZoneId>() {
			@Override
			public ZoneId deserialize(
				final JsonElement json, 
				final Type typeOfSrc,
				final JsonDeserializationContext context
			) throws JsonParseException {
				return ZoneId.of(json.getAsJsonPrimitive().getAsString());
			}
		});
		
		return gsonBuilder.create();
	}

	@Provides @RequestScoped public ExecutorService getThreadPool() {
		return Executors.newCachedThreadPool(ThreadManager.currentRequestThreadFactory());
	}

	@Provides @RequestScoped public ListeningExecutorService getListeningExecService(final Provider<ExecutorService> threadPool) {
		return MoreExecutors.listeningDecorator(threadPool.get());
	}
	
	@Provides @RequestScoped public GitkitUser getGitkitUser(
		final GitkitClient gitkitClient,
		final HttpServletRequest req
	) throws 
		GitkitClientException 
	{
		final GitkitUser gitkitUser = 
			gitkitClient.validateTokenInRequest(req);
		
		if (gitkitUser == null) {
			throw new NotLoggedInException();
		}
		
		return gitkitUser;
	}
	
	@Provides @Singleton @Named("retriable-exceptions") public HashSet<Class<?>> getRetriableExceptions() {
		final HashSet<Class<?>> retriableExceptions =
			new HashSet<>();
		
		retriableExceptions.add(TransientFailureException.class);
		retriableExceptions.add(DatastoreTimeoutException.class);
		retriableExceptions.add(DatastoreFailureException.class);
		retriableExceptions.add(ConcurrentModificationException.class);
		
		retriableExceptions.add(IOException.class);
		retriableExceptions.add(SocketTimeoutException.class);
		retriableExceptions.add(SSLHandshakeException.class);
		
		return retriableExceptions;
	}
	
	@Provides @Singleton public GitkitClient getGitkitClient(
		final KeyStore keyStore,
		final ServletContext context
	) {
		return
			GitkitClient.newBuilder()
			.setGoogleClientId(keyStore.getGoogleClientId())
			.setServiceAccountEmail(keyStore.getGoogleServiceAccountEmail())
			.setKeyStream(context.getResourceAsStream("/WEB-INF/bookstuf-backend-24f631d04e28.p12"))
			.setWidgetUrl("/gitkit")
			.setCookieName("gtoken")
			.build();
	}
}
