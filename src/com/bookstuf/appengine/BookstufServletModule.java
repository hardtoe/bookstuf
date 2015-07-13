package com.bookstuf.appengine;

import com.bookstuf.web.StripeServlet;
import com.bookstuf.web.GitkitWidgetServlet;
import com.google.inject.servlet.ServletModule;

class BookstufServletModule extends ServletModule {
	@Override
	protected void configureServlets() {
		serve("/stripe").with(StripeServlet.class);
		serve("/gitkit").with(GitkitWidgetServlet.class);
	}
}