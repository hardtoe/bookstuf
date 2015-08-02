package com.bookstuf.appengine;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;

@Singleton
public class HttpsRedirectFilter implements Filter {
	public void doFilter(
		final ServletRequest request, 
		final ServletResponse response, 
		final FilterChain chain
	) throws 
		IOException, 
		ServletException 
	{
		if (request.isSecure()) {      
			chain.doFilter(request, response);               
			
		} else {  
			final HttpServletRequest httpReq = (HttpServletRequest) request; 
			final HttpServletResponse httpRsp = (HttpServletResponse) response;
			
			String redirectTarget = httpReq.getRequestURL().toString();
			
			redirectTarget = redirectTarget.replaceFirst("http", "https"); 
			redirectTarget = redirectTarget.replaceFirst("//bookstuf.com", "//www.bookstuf.com"); 
			
			httpRsp.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
			httpRsp.setHeader("Location", redirectTarget);
		}  
	}

	@Override
	public void destroy() {
		// do nothing
	}

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		// do nothing
	}
}