package com.bookstuf.web.booking;

@SuppressWarnings("serial")
public class RequestError extends RuntimeException {
	public RequestError(final String message) {
		super(message);
	}
}
