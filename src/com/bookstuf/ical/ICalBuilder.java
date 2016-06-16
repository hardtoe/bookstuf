package com.bookstuf.ical;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

public class ICalBuilder {
	private static final String END = "END";
	private static final String METHOD = "METHOD";
	private static final String CALSCALE = "CALSCALE";
	private static final String VERSION = "VERSION";
	private static final String PRODID = "PRODID";
	private static final String VCALENDAR = "VCALENDAR";
	private static final String BEGIN = "BEGIN";

	private final DateTimeFormatter TIME_FORMAT = 
		DateTimeFormatter.ofPattern("yyyyMMdd'T'kkmmss");
	
	private final ArrayList<Property> data;
	
	private Property property(
		final String name, 
		final String value
	) {
		final Property property = 
			new Property(name, value);
		
		data.add(property);
		
		return property;
	}
	
	ICalBuilder() {
		this.data = new ArrayList<Property>();
		property(BEGIN, VCALENDAR);
	}
	
	/**
	 * Any text that describes the product and version
	 * and that is generally assured of being unique.
	 */
	public ICalBuilder prodid(
		final String entity, 
		final String product
	) {
		property(PRODID, "-//" + entity + "//" + product + "//EN");
		return this;
	}
	
	public ICalBuilder version(
		final String version
	) {
		property(VERSION, version);
		return this;
	}
	
	public ICalBuilder calscale(
		final String calscale
	) {
		property(CALSCALE, calscale);
		return this;
	}
	
	public ICalBuilder method(
		final String method
	) {
		property(METHOD, method);
		return this;
	}
	
	public ICalBuilder calname(final String calname) {
		property("X-WR-CALNAME", calname);
		return this;
	}
	
	/**
	 * A "VEVENT" calendar component is a grouping of component properties, and
	 * possibly including "VALARM" calendar components, that represents a
	 * scheduled amount of time on a calendar. For example, it can be an
	 * activity; such as a one-hour long, department meeting from 8:00 AM to
	 * 9:00 AM, tomorrow. Generally, an event will take up time on an individual
	 * calendar. Hence, the event will appear as an opaque interval in a search
	 * for busy time. Alternately, the event can have its Time Transparency set
	 * to "TRANSPARENT" in order to prevent blocking of the event in searches
	 * for busy time.
	 * 
	 * The "VEVENT" is also the calendar component used to specify an
	 * anniversary or daily reminder within a calendar. These events have a DATE
	 * value type for the "DTSTART" property instead of the default data type of
	 * DATE-TIME. If such a "VEVENT" has a "DTEND" property, it MUST be
	 * specified as a DATE value also. The anniversary type of "VEVENT" can span
	 * more than one date (i.e, "DTEND" property value is set to a calendar date
	 * after the "DTSTART" property value).
	 * 
	 * The "DTSTART" property for a "VEVENT" specifies the inclusive start of
	 * the event. For recurring events, it also specifies the very first
	 * instance in the recurrence set. The "DTEND" property for a "VEVENT"
	 * calendar component specifies the non-inclusive end of the event. For
	 * cases where a "VEVENT" calendar component specifies a "DTSTART" property
	 * with a DATE data type but no "DTEND" property, the events non-inclusive
	 * end is the end of the calendar date specified by the "DTSTART" property.
	 * For cases where a "VEVENT" calendar component specifies a "DTSTART"
	 * property with a DATE-TIME data type but no "DTEND" property, the event
	 * ends on the same calendar date and time of day specified by the "DTSTART"
	 * property. The "VEVENT" calendar component cannot be nested within another
	 * calendar component. However, "VEVENT" calendar components can be related
	 * to each other or to a "VTODO" or to a "VJOURNAL" calendar component with
	 * the "RELATED-TO" property.
	 */
	public EventBuilder beginEvent() {
		return new EventBuilder();
	}
	
	public class EventBuilder {
		private static final String DESCRIPTION = "DESCRIPTION";
		private static final String SUMMARY = "SUMMARY";
		private static final String DTSTAMP = "DTSTAMP";
		private static final String DTEND = "DTEND";
		private static final String DTSTART = "DTSTART";
		private static final String UID = "UID";
		private static final String VEVENT = "VEVENT";

		private EventBuilder() {
			property(BEGIN, VEVENT);
		}
		
		public EventBuilder uid(final String uid) {
			property(UID, uid);
			return this;
		}
		
		public EventBuilder dtstart(final LocalDateTime t) {
			property(DTSTART, ICalBuilder.this.toString(t));
			return this;
		}
		
		public EventBuilder dtstart(final ZonedDateTime t) {
			property(DTSTART, ICalBuilder.this.toString(t));
			return this;
		}
		
		public EventBuilder dtend(final LocalDateTime t) {
			property(DTEND, ICalBuilder.this.toString(t));
			return this;
		}
		
		public EventBuilder dtend(final ZonedDateTime t) {
			property(DTEND, ICalBuilder.this.toString(t));
			return this;
		}
		
		public EventBuilder dtstamp(final LocalDateTime t) {
			property(DTSTAMP, ICalBuilder.this.toString(t));
			return this;
		}
		
		public EventBuilder dtstamp(final ZonedDateTime t) {
			property(DTSTAMP, ICalBuilder.this.toString(t));
			return this;
		}
		
		public EventBuilder summary(final String summary) {
			property(SUMMARY, summary);
			return this;
		}
		
		public EventBuilder description(final String description) {
			property(DESCRIPTION, description);
			return this;
		}
		
		public EventBuilder location(final String location) {
			property("LOCATION", location);
			return this;
		}
		
		/**
		 * The property value specifies latitude and longitude, in that order
		 * (i.e., "LAT LON" ordering). The longitude represents the location
		 * east or west of the prime meridian as a positive or negative real
		 * number, respectively. The longitude and latitude values MAY be
		 * specified up to six decimal places, which will allow for accuracy to
		 * within one meter of geographical position. Receiving applications
		 * MUST accept values of this precision and MAY truncate values of
		 * greater precision.
		 * 
		 * Values for latitude and longitude shall be expressed as decimal
		 * fractions of degrees. Whole degrees of latitude shall be represented
		 * by a two-digit decimal number ranging from 0 through 90. Whole
		 * degrees of longitude shall be represented by a decimal number ranging
		 * from 0 through 180. When a decimal fraction of a degree is specified,
		 * it shall be separated from the whole number of degrees by a decimal
		 * point.
		 * 
		 * Latitudes north of the equator shall be specified by a plus sign (+),
		 * or by the absence of a minus sign (-), preceding the digits
		 * designating degrees. Latitudes south of the Equator shall be
		 * designated by a minus sign (-) preceding the digits designating
		 * degrees. A point on the Equator shall be assigned to the Northern
		 * Hemisphere.
		 * 
		 * Longitudes east of the prime meridian shall be specified by a plus
		 * sign (+), or by the absence of a minus sign (-), preceding the digits
		 * designating degrees. Longitudes west of the meridian shall be
		 * designated by minus sign (-) preceding the digits designating
		 * degrees. A point on the prime meridian shall be assigned to the
		 * Eastern Hemisphere. A point on the 180th meridian shall be assigned
		 * to the Western Hemisphere. One exception to this last convention is
		 * permitted. For the special condition of describing a band of latitude
		 * around the earth, the East Bounding Coordinate data element shall be
		 * assigned the value +180 (180) degrees.
		 * 
		 * Any spatial address with a latitude of +90 (90) or -90 degrees will
		 * specify the position at the North or South Pole, respectively. The
		 * component for longitude may have any legal value.
		 * 
		 * With the exception of the special condition described above, this
		 * form is specified in Department of Commerce, 1986, Representation of
		 * geographic point locations for information interchange (Federal
		 * Information Processing Standard 70-1): Washington, Department of
		 * Commerce, National Institute of Standards and Technology.
		 * 
		 * The simple formula for converting degrees-minutes-seconds into
		 * decimal degrees is:
		 * 
		 * decimal = degrees + minutes/60 + seconds/3600.
		 */
		public EventBuilder geo(final BigDecimal latitude, final BigDecimal longitude) {
			property("GEO", latitude.toPlainString() + ";" + longitude.toPlainString());
			return this;
		}
		
		/**
		 * The property MUST only be specified within calendar components to
		 * specify participants, non-participants and the chair of a group
		 * scheduled calendar entity. The property is specified within an
		 * "EMAIL" category of the "VALARM" calendar component to specify an
		 * email address that is to receive the email type of iCalendar alarm.
		 * 
		 * The property parameter CN is for the common or displayable name
		 * associated with the calendar address; ROLE, for the intended role
		 * that the attendee will have in the calendar component; PARTSTAT, for
		 * the status of the attendee's participation; RSVP, for indicating
		 * whether the favor of a reply is requested; CUTYPE, to indicate the
		 * type of calendar user; MEMBER, to indicate the groups that the
		 * attendee belongs to; DELEGATED-TO, to indicate the calendar users
		 * that the original request was delegated to; and DELEGATED-FROM, to
		 * indicate whom the request was delegated from; SENT-BY, to indicate
		 * whom is acting on behalf of the ATTENDEE; and DIR, to indicate the
		 * URI that points to the directory information corresponding to the
		 * attendee. These property parameters can be specified on an "ATTENDEE"
		 * property in either a "VEVENT", "VTODO" or "VJOURNAL" calendar
		 * component. They MUST not be specified in an "ATTENDEE" property in a
		 * "VFREEBUSY" or "VALARM" calendar component. If the LANGUAGE property
		 * parameter is specified, the identified language applies to the CN
		 * parameter.	
		 * 
		 * A recipient delegated a request MUST inherit the RSVP and ROLE values
		 * from the attendee that delegated the request to them.
		 * 
		 * Multiple attendees can be specified by including multiple "ATTENDEE"
		 * properties within the calendar component.
		 */
		public EventBuilder attendee(final String name, final String email) {
			final Property p =
				property("ATTENDEE", "MAILTO:" + email)
					.param("ROLE", "REQ-PARTICIPANT")
					.param("PARTSTAT", "ACCEPTED");
			
			if (p != null) {
				p.param("CN", name);
			}
			
			return this;
		}
		
		public EventBuilder status(final EventStatus status) {
			property("STATUS", status.toString());
			return this;
		}
		
		/**
		 * The property is specified within the "VEVENT", "VTODO",
		 * "VJOURNAL calendar components to specify the organizer of a group scheduled calendar entity. The property is specified within the "
		 * VFREEBUSY
		 * " calendar component to specify the calendar user requesting the free or busy time. When publishing a "
		 * VFREEBUSY" calendar component, the property is used to specify the
		 * calendar that the published busy time came from.
		 * 
		 * The property has the property parameters CN, for specifying the
		 * common or display name associated with the "Organizer", DIR, for
		 * specifying a pointer to the directory information associated with the
		 * "Organizer", SENT-BY, for specifying another calendar user that is
		 * acting on behalf of the "Organizer". The non-standard parameters may
		 * also be specified on this property. If the LANGUAGE property
		 * parameter is specified, the identified language applies to the CN
		 * parameter value.
		 */
		public EventBuilder organizer(final String name, final String email) {
			property("ORGANIZER", "MAILTO:" + email)
				.param("CN", name);
			
			return this;
		}

		public AlarmBuilder beginAlarm() {
			return new AlarmBuilder();
		}
		
		public class AlarmBuilder<Parent> {
			private AlarmBuilder() {
				property(BEGIN, "VALARM");
			}
			
			public AlarmBuilder action(final AlarmAction action) {
				property("ACTION", action.toString());
				return this;
			}
			
			public AlarmBuilder trigger(final ZonedDateTime time) {
				property("TRIGGER", ICalBuilder.this.toString(time))
					.param("VALUE", "DATE-TIME");
				
				return this;
			}
			
			public AlarmBuilder description(final String description) {
				property("DESCRIPTION", description);
				return this;
			}
			
			public EventBuilder endAlarm() {
				property(END, "VALARM");
				return EventBuilder.this;
			}
		}
		
		public ICalBuilder endEvent() {
			property(END, VEVENT);
			return ICalBuilder.this;
		}
	}
	
	public ICal endCalendar() {
		property(END, VCALENDAR);
		return new ICal(data);
	}
	
	private String toString(final LocalDateTime t) {
		return t.format(TIME_FORMAT);
	}

	private String toString(final ZonedDateTime t) {
		return toString(t.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()) + "Z";
	}
}
