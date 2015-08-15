package com.bookstuf.web.billing;

import com.bookstuf.datastore.Bill;
import com.bookstuf.datastore.ConsumerDailyAgenda;
import com.bookstuf.datastore.ConsumerInformation;
import com.bookstuf.datastore.DailyAgenda;
import com.bookstuf.datastore.PaymentStatus;
import com.bookstuf.datastore.ProfessionalInformation;
import com.bookstuf.datastore.ProfessionalPrivateInformation;
import com.bookstuf.web.pipeline.AbstractPipelineServlet;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.mapreduce.GoogleCloudStorageFileSet;
import com.google.appengine.tools.mapreduce.MapJob;
import com.google.appengine.tools.mapreduce.MapReduceResult;
import com.google.appengine.tools.mapreduce.MapReduceSettings;
import com.google.appengine.tools.mapreduce.MapSettings;
import com.google.appengine.tools.mapreduce.MapSpecification;
import com.google.appengine.tools.mapreduce.Output;
import com.google.appengine.tools.mapreduce.OutputWriter;
import com.google.appengine.tools.mapreduce.bigqueryjobs.BigQueryLoadGoogleCloudStorageFilesJob;
import com.google.appengine.tools.mapreduce.bigqueryjobs.BigQueryLoadJobReference;
import com.google.appengine.tools.mapreduce.impl.BigQueryMarshallerByType;
import com.google.appengine.tools.mapreduce.inputs.ConsecutiveLongInput;
import com.google.appengine.tools.mapreduce.inputs.DatastoreInput;
import com.google.appengine.tools.mapreduce.inputs.DatastoreKeyInput;
import com.google.appengine.tools.mapreduce.outputs.BigQueryGoogleCloudStorageStoreOutput;
import com.google.appengine.tools.mapreduce.outputs.BigQueryStoreResult;
import com.google.appengine.tools.mapreduce.outputs.NoOutput;
import com.google.appengine.tools.pipeline.FutureValue;
import com.google.appengine.tools.pipeline.Job0;
import com.google.appengine.tools.pipeline.Job1;
import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;
import com.google.appengine.tools.pipeline.Value;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.lang.Integer.parseInt;

import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.appidentity.AppIdentityServiceFailureException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;

import static com.google.appengine.api.datastore.Query.CompositeFilterOperator.*;

import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import com.googlecode.objectify.ObjectifyService;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class SchemaUpdateServlet extends AbstractPipelineServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public String startJob(
		final PipelineService service, 
		final JobParamValues params
	) throws 
		Exception 
	{
		final Class<?> entityType =
			Class.forName(params.getString("entity-type"));
		
	    final Query query =
        	new Query(entityType.getSimpleName());
	    
		return service.startNewPipeline(
			new MapJob<Key, Void, Void>(
				new MapSpecification.Builder<Key, Void, Void>()
					.setJobName("schema update servlet")
					.setInput(new DatastoreKeyInput(query, 10))
					.setMapper(new SchemaUpdateMapper(entityType))
					.setOutput(new NoOutput<Void, Void>())
					.build(), 
					
				new MapReduceSettings.Builder()
					.setWorkerQueueName("mapreduce-workers")
					.setBucketName("bookstuf-mapreduce")
					.build()));
	}
	
	@Override
	public JobParams createJobParamsDefinition() {
		return params(
			"schema update servlet", 
			"/secure/schema-update", 
			select("entity-type", 
				ProfessionalInformation.class.getCanonicalName(), 
				ConsumerInformation.class.getCanonicalName(), 
				DailyAgenda.class.getCanonicalName(), 
				ConsumerDailyAgenda.class.getCanonicalName(), 
				ProfessionalPrivateInformation.class.getCanonicalName(), 
				Bill.class.getCanonicalName()));
	}
}