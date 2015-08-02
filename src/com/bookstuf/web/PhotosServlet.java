package com.bookstuf.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.googlecode.objectify.ObjectifyService.ofy;

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
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;

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

	@Publish(withAutoRetryMillis = 30000) @AsTransaction
	private void delete(
		@Param("url") final String url
	) {       	    
		final UserInformation userInformation =
			userService.getCurrentUserInformation();

		final BlobKey blobKey = 
			userInformation.removePhoto(url);
		
		ofy().save().entity(userInformation);
		
		if (blobKey != null) {
			blobstoreService.delete(blobKey);
		}
	}
	
	@Publish(withAutoRetryMillis = 30000) @AsTransaction
	private void upload(
		final HttpServletRequest req,
		final HttpServletResponse rsp
	) throws 
		IOException 
	{
        boolean tooManyPhotos = false;

		final UserInformation userInformation =
			userService.getCurrentUserInformation();
		
        final Map<String, List<BlobKey>> blobs = 
        	blobstoreService.getUploads(req);
        
        final List<BlobKey> blobKeys = 
        	blobs.get("file");
        
        for (final BlobKey blobKey : blobKeys) {
        	final ServingUrlOptions urlOptions = 
        		ServingUrlOptions.Builder
        		.withBlobKey(blobKey)
        		.secureUrl(true);
        	
        	if (userInformation.hasPhotoBlobKey(blobKey)) {
        		// do nothing, this photo has previously been added
        		
        	} else if (userInformation.getPhotoUrls().size() < MAX_PHOTOS) {
				final String url =
        			imagesService.getServingUrl(urlOptions);
	        			
				userInformation.addPhoto(blobKey, url);
				
        	} else {
        		blobstoreService.delete(blobKey);
        		tooManyPhotos = true;
        	}
        }
		
        ofy().save().entity(userInformation);

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
