package com.bookstuf.appengine;

import com.bookstuf.web.BookingServlet;
import com.bookstuf.web.LoginServlet;
import com.bookstuf.web.LogoutServlet;
import com.bookstuf.web.ServicesPageServlet;
import com.bookstuf.web.PhotosServlet;
import com.bookstuf.web.StripeServlet;
import com.bookstuf.web.GitkitWidgetServlet;
import com.bookstuf.web.ProfessionalInformationServlet;
import com.bookstuf.web.WarmupServlet;
import com.google.inject.servlet.ServletModule;
import com.googlecode.objectify.ObjectifyFilter;

class BookstufServletModule extends ServletModule {
	@Override
	protected void configureServlets() {
		filter("/*").through(ObjectifyFilter.class);
		filter("/*").through(HttpsRedirectFilter.class);
		
		serve("/gitkit").with(GitkitWidgetServlet.class);
		serve("/login").with(LoginServlet.class);
		serve("/logout").with(LogoutServlet.class);
		serve("/user/*").with(ProfessionalInformationServlet.class);
		serve("/photos/*").with(PhotosServlet.class);
		serve("/book").with(BookingServlet.class);
		serve("/stripe").with(StripeServlet.class);
		
		serve("/_ah/warmup").with(WarmupServlet.class);
		
		serve("/*").with(ServicesPageServlet.class);
	}
}