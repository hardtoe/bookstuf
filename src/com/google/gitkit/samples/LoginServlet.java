package com.google.gitkit.samples;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import javax.management.RuntimeErrorException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import com.google.identitytoolkit.GitkitClient;
import com.google.identitytoolkit.GitkitClientException;
import com.google.identitytoolkit.GitkitUser;

/*
 public static void main(String[] args) throws Exception {
 Server server = new Server(4567);
 ServletHandler servletHandler = new ServletHandler();
 SessionHandler sessionHandler = new SessionHandler();
 sessionHandler.setHandler(servletHandler);
 server.setHandler(sessionHandler);
 servletHandler.addServletWithMapping(LoginServlet.class, "/login");
 servletHandler.addServletWithMapping(WidgetServlet.class, "/gitkit");
 servletHandler.addServletWithMapping(LoginServlet.class, "/");
 server.start();
 server.join();
 }
 */
public class LoginServlet extends HttpServlet {
	@Override
	protected void doGet(
		HttpServletRequest request,
		HttpServletResponse response
	) throws 
		ServletException, 
		IOException 
	{
		try {
			final GitkitClient gitkitClient = 
				GitkitClient.newBuilder()
				.setGoogleClientId("1022706286728-22r7bucp7mp3kk7vdhk7kvda5pohkgcg.apps.googleusercontent.com")
				.setServiceAccountEmail("1022706286728-hebtee767c8jen3odtbcvlle7ok6rh7k@developer.gserviceaccount.com")
				.setKeyStream(getServletContext().getResourceAsStream("/WEB-INF/bookstuf-backend-24f631d04e28.p12"))
				.setWidgetUrl("/gitkit")
				.setCookieName("gtoken")
				.build();
					

			final GitkitUser gitkitUser = 
				gitkitClient.validateTokenInRequest(request);
			
			String userInfo = null;
			if (gitkitUser != null) {
				userInfo 
					= "Welcome back!<br><br> Email: "
					+ gitkitUser.getEmail() + "<br> Id: "
					+ gitkitUser.getLocalId() + "<br> Provider: "
					+ gitkitUser.getCurrentProvider();
			}

			final InputStream indexTemplateStream = 
				getServletContext().getResourceAsStream("/WEB-INF/templates/index.html");
			
			response.getWriter().print(
					new Scanner(indexTemplateStream, "UTF-8")
							.useDelimiter("\\A")
							.next()
							.replaceAll(
									"WELCOME_MESSAGE",
									userInfo != null ? userInfo
											: "You are not logged in")
							.toString());
			
			response.setStatus(HttpServletResponse.SC_OK);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}