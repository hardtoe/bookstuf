package com.bookstuf.web;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.stringtemplate.v4.NoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupDir;
import org.stringtemplate.v4.STGroupFile;

import com.bookstuf.appengine.UserService;
import com.bookstuf.datastore.UserInformation;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class ServicesPageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final STGroup templates;
	private final Provider<UserService> userService;
	
	@Inject ServicesPageServlet(
		final Provider<UserService> userService
	) {
		this.userService = userService;
		this.templates = new STGroupFile("WEB-INF/templates/ServicesPage.stg", '$', '$');
	}
	
	public void doGet(
		final HttpServletRequest req, 
		final HttpServletResponse rsp
	) throws IOException {
		final String handle =
			req.getPathInfo().substring(1);
		
		final UserInformation userInformation =
			userService.get().getUserInformationByHandle(handle);
		
		final ST t =
			templates.getInstanceOf("servicesPage");
		
		t.add("info", userInformation);
		
		t.write(new NoIndentWriter(rsp.getWriter()));
	}
}
