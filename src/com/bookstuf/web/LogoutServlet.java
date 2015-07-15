package com.bookstuf.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.inject.Singleton;

@Singleton
@SuppressWarnings("serial")
public class LogoutServlet extends HttpServlet {
	@Override
	protected void doGet(
		final HttpServletRequest request,
		final HttpServletResponse response
	) throws
		ServletException, 
		IOException 
	{
		response.setStatus(302);
		response.addHeader("Location", getCookie(request, "destination"));
		response.addCookie(new Cookie("destination", ""));
	}
	
	private String getCookie(
		final HttpServletRequest req, 
		final String name
	) {
		for (final Cookie c : req.getCookies()) {
			if (c.getName().equals(name)) {
				return c.getValue();
			}
		}
		
		return "";
	}
}
