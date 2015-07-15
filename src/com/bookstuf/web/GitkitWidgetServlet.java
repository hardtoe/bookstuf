package com.bookstuf.web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Scanner;

import com.google.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class GitkitWidgetServlet extends HttpServlet {
	private static final long serialVersionUID = -2012466413430655666L;

	@Override
	protected void doGet(
		final HttpServletRequest request,
		final HttpServletResponse response
	) throws
		ServletException, 
		IOException 
	{
		response.setContentType("text/html");

		final StringBuilder builder = new StringBuilder();
		String line;
		try {
			while ((line = request.getReader().readLine()) != null) {
				builder.append(line);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		String postBody = URLEncoder.encode(builder.toString(), "UTF-8");

		try {
			response.getWriter().print(
					new Scanner(getServletContext().getResourceAsStream("/WEB-INF/templates/gitkit-widget.html"),
							"UTF-8")
							.useDelimiter("\\A")
							.next()
							.replaceAll("JAVASCRIPT_ESCAPED_POST_BODY",
									postBody).toString());
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().print(e.toString());
		}
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}