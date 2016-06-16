package com.bookstuf.ical;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

class Property {
	private final String name;
	private final String value;
	
	private final ArrayList<Param> params;
	
	public Property(
		final String name, 
		final String value
	) {
		this.params = new ArrayList<Property.Param>();
		
		this.name = name;
		this.value = value;
	}
	
	public Property param(final String name, final String value) {
		if (name != null && value != null) {
			params.add(new Param(name, value));
		}
		
		return this;
	}
	
	private class Param {
		private final String name;
		private final String value;
		
		public Param(
			final String name,
			final String value
		) {
			this.name = name;
			this.value = value;
		}

		public void output(final Writer w) throws IOException {
			w.write(";");
			w.write(name);
			w.write("=");
			w.write(value);
		}
	}

	public void output(Writer w) throws IOException {
		w.write(name);
		
		for (final Param p : params) {
			p.output(w);
		}
		
		w.write(":");
		w.write(value);
	}
}