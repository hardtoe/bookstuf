package com.bookstuf.appengine;

import com.bookstuf.web.ICalServlet;
import com.bookstuf.web.LoginServlet;
import com.bookstuf.web.LogoutServlet;
import com.bookstuf.web.ProfilePageServlet;
import com.bookstuf.web.PhotosServlet;
import com.bookstuf.web.StripeServlet;
import com.bookstuf.web.GitkitWidgetServlet;
import com.bookstuf.web.ProfessionalInformationServlet;
import com.bookstuf.web.WarmupServlet;
import com.bookstuf.web.billing.BillingCronServlet;
import com.bookstuf.web.billing.SchemaUpdateServlet;
import com.bookstuf.web.booking.BookingServlet;
import com.bookstuf.web.card.CardServlet;
import com.google.appengine.tools.mapreduce.MapReduceServlet;
import com.google.appengine.tools.mapreduce.servlets.ShufflerServlet;
import com.google.appengine.tools.pipeline.impl.servlets.PipelineServlet;
import com.google.inject.servlet.ServletModule;
import com.googlecode.objectify.ObjectifyFilter;

class BookstufServletModule extends ServletModule {
	@Override
	protected void configureServlets() {
		filter("/*").through(ObjectifyFilter.class);
		filter("/*").through(HttpsRedirectFilter.class);
		

		serve("/_ah/pipeline/*").with(PipelineServlet.class);
		serve("/mapreduce/*").with(MapReduceServlet.class);
		//serve("/shufflerServlet/*").with(ShufflerServlet.class);
		
		//serve("/secure/billing-cron").with(BillingCronServlet.class);
		serve("/secure/schema-update").with(SchemaUpdateServlet.class);

		
		serve("/gitkit").with(GitkitWidgetServlet.class);
		serve("/login").with(LoginServlet.class);
		serve("/logout").with(LogoutServlet.class);
		serve("/user/*").with(ProfessionalInformationServlet.class);
		serve("/photos/*").with(PhotosServlet.class);
		serve("/booking/*").with(BookingServlet.class);
		serve("/cards/*").with(CardServlet.class);
		serve("/stripe").with(StripeServlet.class);
		
		serve("/_ah/warmup").with(WarmupServlet.class);

		serveRegex("/[^/]+/cal.ics").with(ICalServlet.class);
		serveRegex("/[^/]+").with(ProfilePageServlet.class);
	}
}