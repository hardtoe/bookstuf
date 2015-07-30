package com.bookstuf.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slim3.datastore.Datastore;

import com.bookstuf.appengine.NotLoggedInException;
import com.bookstuf.appengine.UserService;
import com.bookstuf.datastore.UserInformation;
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
	private final BlobstoreService blobstoreService;
	private final ImagesService imagesService;
	
	private static final int MAX_PHOTOS = 50;
	
	@Inject PhotosServlet(
		final Logger logger,
		final UserService userService
	) {
		this.logger = logger;
		this.userService = userService;
		this.blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
		this.imagesService = ImagesServiceFactory.getImagesService();
	}

	@Publish(autoRetryMillis = 30000)
	private void delete(
		@Param("url") final String url
	) {       	    
		final Transaction t =
			Datastore.beginTransaction();
				
		BlobKey blobKey = null;
		
		try {
			final UserInformation userInformation =
				userService.getCurrentUserInformation(t);
	        
			blobKey = userInformation.removePhoto(url);
			
			Datastore.put(t, userInformation);
			
			t.commit();
			
		} finally {
			if (t.isActive()) {
				t.rollback();
			}
		}

		// only delete the image data if the transaction succeeds
		if (blobKey != null) {
			blobstoreService.delete(blobKey);
		}
	}
	
	@Publish(autoRetryMillis = 30000)
	private void upload(
		final HttpServletRequest req,
		final HttpServletResponse rsp
	) throws 
		IOException 
	{
        boolean tooManyPhotos = false;
        
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
	        	
	        	if (userInformation.getPhotoUrls().size() < MAX_PHOTOS) {
					final String url =
	        			imagesService.getServingUrl(urlOptions);
		        			
					userInformation.addPhoto(blobKey, url);
					
	        	} else {
	        		blobstoreService.delete(blobKey);
	        		tooManyPhotos = true;
	        	}
	        }
			
			Datastore.put(t, userInformation);
			
			t.commit();
			
		} finally {
			if (t.isActive()) {
				t.rollback();
			}
		}
		
		if (tooManyPhotos) {
			rsp.setStatus(500);
			rsp.getOutputStream().println("Picture limit of 50 exceeded");
			
		} else {
			rsp.getOutputStream().println("Success");
		}
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
