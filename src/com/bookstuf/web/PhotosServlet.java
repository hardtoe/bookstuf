package com.bookstuf.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slim3.datastore.Datastore;

import com.bookstuf.GsonHelper;
import com.bookstuf.appengine.NotLoggedInException;
import com.bookstuf.appengine.UserService;
import com.bookstuf.datastore.User;
import com.bookstuf.datastore.UserInformation;
import com.bookstuf.datastore.UserServices;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.identitytoolkit.GitkitClientException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@SuppressWarnings("serial")
public class PhotosServlet extends RpcServlet {
	private final Logger logger;
	private final UserService userService;
	private final GsonHelper gsonHelper;
	private final BlobstoreService blobstoreService;
	private final ImagesService imagesService;
	
	@Inject PhotosServlet(
		final Logger logger,
		final UserService userService,
		final GsonHelper gsonHelper
	) {
		this.logger = logger;
		this.userService = userService;
		this.gsonHelper = gsonHelper;
		this.blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
		this.imagesService = ImagesServiceFactory.getImagesService();
	}
	
	@Publish(autoRetryMillis = 30000)
	private String upload(
		final HttpServletRequest req
	) {
		final Transaction t =
			Datastore.beginTransaction();
				
		try {
			final UserInformation userInformation =
				userService.getCurrentUserInformation(t);
			
	        final Map<String, List<BlobKey>> blobs = 
	        	blobstoreService.getUploads(req);
	        
	        final List<BlobKey> blobKeys = 
	        	blobs.get("file");
			
	        for (final BlobKey blobKey : blobKeys) {
	        	final ServingUrlOptions urlOptions = 
	        		ServingUrlOptions.Builder
	        		.withBlobKey(blobKey)
	        		.secureUrl(true);
	        	
				final String url =
        			imagesService.getServingUrl(urlOptions);
	        			
				userInformation.getPhotoUrls().add(url);
	        }
			
			Datastore.put(userInformation);
			
			t.commit();
			
		} finally {
			if (t.isActive()) {
				t.rollback();
			}
		}
		
		return "";
	}
	
	@Publish
	private String getUploadUrl() {
		return blobstoreService.createUploadUrl("/photos/upload");
	}

	@Default
	private String notFound(final HttpServletResponse response) {
		response.setStatus(404);
		return "Unknown Operation";
	}
	
	@ExceptionHandler(NotLoggedInException.class) 
	private void handleNotLoggedInException(
		final HttpServletResponse response
	) throws 
		IOException 
	{
		response.getWriter().println("Not logged In!");
	}
	
	@ExceptionHandler(GitkitClientException.class) 
	private void handleGitkitClientException(
		final HttpServletResponse response
	) throws 
		IOException 
	{
		logger.log(Level.SEVERE, "Could not validate gitkit user.", getCurrentException());
		response.getWriter().println("Not logged In!");
	}
}
