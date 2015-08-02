package com.bookstuf.appengine;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.bookstuf.datastore.User;
import com.bookstuf.datastore.UserInformation;

import com.googlecode.objectify.ObjectifyService;

public class BookstufServletContextListener implements ServletContextListener {
	@Override
	public void contextInitialized(final ServletContextEvent e) {
		ObjectifyService.register(UserInformation.class);
		ObjectifyService.register(User.class);
	}
	
	@Override
	public void contextDestroyed(final ServletContextEvent e) {
		// do nothing
	}
}