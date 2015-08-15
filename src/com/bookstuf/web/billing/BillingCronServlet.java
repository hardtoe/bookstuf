package com.bookstuf.web.billing;

import com.bookstuf.datastore.DailyAgenda;
import com.bookstuf.datastore.PaymentStatus;
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

import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class BillingCronServlet /* extends HttpServlet */ {
/*	private static final long serialVersionUID = 1L;


  private static final Logger log = Logger.getLogger(BillingCronServlet.class.getName());

  private final MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
  private final UserService userService = UserServiceFactory.getUserService();
  private final SecureRandom random = new SecureRandom();

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	final String DATASTORE_TYPE = "DailyAgenda";
	final String GCS_BUCKET = "bookstuf-mapreduce";
    final int shardCount = 10;
    
    final PipelineService service = 
    	PipelineServiceFactory.newPipelineService();
    
    final Date now =
    	Calendar.getInstance().getTime();
    
    final Query query =
    	new Query(DailyAgenda.class.getSimpleName())
    	.setFilter(
    		and(
    			new FilterPredicate("hasPendingBookings", FilterOperator.EQUAL, true),
    			new FilterPredicate("date", FilterOperator.LESS_THAN_OR_EQUAL, now)))
    	.addSort("date", SortDirection.ASCENDING);
    
	final String pipelineId = 
		service.startNewPipeline(
			new MapJob<Key, Void, Void>(
				new MapSpecification.Builder<Key, Void, Void>()
					.setJobName("billing cron servlet")
					.setInput(new DatastoreKeyInput(query, 10))
					.setMapper(new BillDailyAgenda())
					.setOutput(new NoOutput<Void, Void>())
					.build(), 
					
				new MapReduceSettings.Builder()
					.setWorkerQueueName("mapreduce-workers")
					.setBucketName("bookstuf-mapreduce")
					.build()));
	
	
    resp.getWriter().println("<a href=\"" + getPipelineStatusUrl(pipelineId) + "\">Pipeline Status</a>");
  }

  private String getPipelineStatusUrl(String pipelineId) {
    return "/_ah/pipeline/status.html?root=" + pipelineId;
  }
*/
}