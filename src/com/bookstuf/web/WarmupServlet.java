package com.bookstuf.web;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;

@Singleton
public class WarmupServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(
		final HttpServletRequest req, 
		final HttpServletResponse rsp
	) throws 
		IOException 
	{
		// warmup stuff
	}
}
