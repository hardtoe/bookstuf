package com.google.gitkit.samples;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WidgetServlet extends HttpServlet {
	@Override
	protected void doGet(
		HttpServletRequest request,
		HttpServletResponse response
	) throws ServletException, IOException {
		response.setContentType("text/html");

		StringBuilder builder = new StringBuilder();
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