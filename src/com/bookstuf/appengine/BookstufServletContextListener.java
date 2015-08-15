package com.bookstuf.appengine;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.bookstuf.datastore.Bill;
import com.bookstuf.datastore.ConsumerInformation;
import com.bookstuf.datastore.DailyAgenda;
import com.bookstuf.datastore.ConsumerDailyAgenda;
import com.bookstuf.datastore.ProfessionalPrivateInformation;
import com.bookstuf.datastore.ProfessionalInformation;
import com.googlecode.objectify.ObjectifyService;

public class BookstufServletContextListener implements ServletContextListener {
	@Override
	public void contextInitialized(final ServletContextEvent e) {
		ObjectifyService.register(ProfessionalInformation.class);
		ObjectifyService.register(ConsumerInformation.class);
		ObjectifyService.register(DailyAgenda.class);
		ObjectifyService.register(ConsumerDailyAgenda.class);
		ObjectifyService.register(ProfessionalPrivateInformation.class);
		ObjectifyService.register(Bill.class);
	}
	
	@Override
	public void contextDestroyed(final ServletContextEvent e) {
		// do nothing
	}
}