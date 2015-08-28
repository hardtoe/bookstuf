package com.bookstuf.web;

import java.io.InputStreamReader;
import java.util.HashMap;

import javax.servlet.ServletContext;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

public class MustacheLoader {
	private final HashMap<String, Mustache> compiledTemplates =
		new HashMap<String, Mustache>();
	
	private final DefaultMustacheFactory mf =
		new DefaultMustacheFactory();
	
	private final ServletContext context;
	
	public MustacheLoader(final ServletContext context) {
		this.context = context;
	}
	
	public Mustache load(final String name) {
		Mustache template =
			compiledTemplates.get(name);
		
		if (template == null) {
			template =
				mf.compile(new InputStreamReader(context.getResourceAsStream("/WEB-INF/templates/" + name)), name);	
			
			compiledTemplates.put(name, template);
		}
		
		return template;
	}
}