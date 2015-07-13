package com.bookstuf.web;
import java.io.IOException;

import com.google.inject.Singleton;
import javax.servlet.http.*;

@Singleton
@SuppressWarnings("serial")
public class BookstufServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");
		resp.getWriter().println("Hello, world");
	}
}
