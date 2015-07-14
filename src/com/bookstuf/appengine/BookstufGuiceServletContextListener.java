package com.bookstuf.appengine;

import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class BookstufGuiceServletContextListener extends GuiceServletContextListener {
	private static final Injector injector = 
		Guice.createInjector(
			new BookstufServletModule(),
			new BookstufLogicModule());
		
	@Override protected Injector getInjector() {
		return injector;
	}
}