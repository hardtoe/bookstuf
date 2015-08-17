package com.bookstuf.datastore;

import org.threeten.bp.LocalDate;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Parent;

@Cache @Entity
public class ConsumerDailyAgenda extends DailyAgenda {
	@Parent Key<ConsumerInformation> consumer;
	
	public void setOwnerAndDate(
		final String gitkitUserId,
		final LocalDate date
	) {
		consumer =
			Key.create(ConsumerInformation.class, gitkitUserId);
		
		ownerAndDate =
			createKeyString(gitkitUserId, date);
	}
	
	public static Key<ConsumerDailyAgenda> createConsumerKey(
		final Key<ConsumerInformation> parentKey,
		final LocalDate date
	) {
		return Key.create(parentKey, ConsumerDailyAgenda.class, createKeyString(parentKey.getName(), date));
	}
}
