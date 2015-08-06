package com.bookstuf.appengine;

import static com.googlecode.objectify.ObjectifyService.ofy;

import com.bookstuf.Memcacheable;
import com.bookstuf.datastore.ProfessionalInformation;
import com.googlecode.objectify.Key;

public final class HandleToProfessionalInformationKeyMemcacheable extends
		Memcacheable<String, Key<ProfessionalInformation>> {
	@Override
	protected Key<ProfessionalInformation> generate(final String handle) {
		return ofy().load().type(ProfessionalInformation.class).filter("handle", handle).keys().first().now();
	}
}