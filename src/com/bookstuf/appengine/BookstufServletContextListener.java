package com.bookstuf.appengine;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

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
		ObjectifyService.factory().getTranslators().add(
			new SimpleTranslatorFactory<LocalDate, String>(LocalDate.class, String.class) {
				@Override
				protected String toDatastore(final LocalDate value) {
					return value.toString();
				}
	
				@Override
				protected LocalDate toPojo(final String value) {
					return LocalDate.parse(value);
				}
			});
		
		ObjectifyService.factory().getTranslators().add(
			new SimpleTranslatorFactory<LocalTime, String>(LocalTime.class, String.class) {
				@Override
				protected String toDatastore(final LocalTime value) {
					return value.toString();
				}
	
				@Override
				protected LocalTime toPojo(final String value) {
					return LocalTime.parse(value);
				}
			});
		
		ObjectifyService.factory().getTranslators().add(
			new SimpleTranslatorFactory<LocalDateTime, String>(LocalDateTime.class, String.class) {
				@Override
				protected String toDatastore(final LocalDateTime value) {
					return value.toString();
				}
	
				@Override
				protected LocalDateTime toPojo(final String value) {
					return LocalDateTime.parse(value);
				}
			});
		
		ObjectifyService.factory().getTranslators().add(
			new SimpleTranslatorFactory<ZoneId, String>(ZoneId.class, String.class) {
				@Override
				protected String toDatastore(final ZoneId value) {
					return value.toString();
				}
	
				@Override
				protected ZoneId toPojo(final String value) {
					return ZoneId.of(value);
				}
			});
		
		ObjectifyService.factory().getTranslators().add(
			new SimpleTranslatorFactory<ZonedDateTime, String>(ZonedDateTime.class, String.class) {
				@Override
				protected String toDatastore(final ZonedDateTime value) {
					return value.toString();
				}
	
				@Override
				protected ZonedDateTime toPojo(final String value) {
					return ZonedDateTime.parse(value);
				}
			});
		
		ObjectifyService.factory().getTranslators().add(
			new SimpleTranslatorFactory<Instant, Long>(Instant.class, Long.class) {
				@Override
				protected Long toDatastore(final Instant value) {
					return value.toEpochMilli();
				}
	
				@Override
				protected Instant toPojo(final Long value) {
					return Instant.ofEpochMilli(value);
				}
			});
		
		ObjectifyService.factory().getTranslators().add(
			new SimpleTranslatorFactory<Duration, Long>(Duration.class, Long.class) {
				@Override
				protected Long toDatastore(final Duration value) {
					return value.toMillis();
				}
	
				@Override
				protected Duration toPojo(final Long value) {
					return Duration.ofMillis(value);
				}
			});
		
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