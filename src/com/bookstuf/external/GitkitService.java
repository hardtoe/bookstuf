package com.bookstuf.external;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.google.identitytoolkit.GitkitClient;
import com.google.identitytoolkit.GitkitClientException;
import com.google.identitytoolkit.GitkitUser;

public class GitkitService {
	private static GitkitClient gitkitClient = null;
	
	public static GitkitUser getUser(
		final ServletContext context,
		final HttpServletRequest request
	) {
		try {
			if (gitkitClient == null) {
				gitkitClient = 
					GitkitClient.newBuilder()
					.setGoogleClientId("1022706286728-22r7bucp7mp3kk7vdhk7kvda5pohkgcg.apps.googleusercontent.com")
					.setServiceAccountEmail("1022706286728-hebtee767c8jen3odtbcvlle7ok6rh7k@developer.gserviceaccount.com")
					.setKeyStream(context.getResourceAsStream("/WEB-INF/bookstuf-backend-24f631d04e28.p12"))
					.setWidgetUrl("/gitkit")
					.setCookieName("gtoken")
					.build();
			}
	
			return gitkitClient.validateTokenInRequest(request);
		} catch (final GitkitClientException e) {
			throw new RuntimeException(e);
		}
	}
}
