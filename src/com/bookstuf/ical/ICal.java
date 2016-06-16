package com.bookstuf.ical;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

public class ICal {
	private final ArrayList<Property> data;
	
	ICal(final ArrayList<Property> data) {
		this.data = data;
	}

	public static ICalBuilder beginCalendar() {
		return new ICalBuilder();
	}
	
	public void output(final Writer w) throws IOException {
		for (final Property p : data) {
			p.output(w);
			w.write("\r\n");
		}
	}
}
