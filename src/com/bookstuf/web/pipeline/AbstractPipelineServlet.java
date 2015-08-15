package com.bookstuf.web.pipeline;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;
import com.google.inject.Inject;

public abstract class AbstractPipelineServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public abstract String startJob(
		final PipelineService service,
		final JobParamValues params) throws Exception;
	
	public abstract JobParams createJobParamsDefinition();

	public static class JobParamValues {
		final Map<String, String[]> values;
		
		private JobParamValues(final Map<String, String[]> values) {
			this.values = values;
		}
		
		public String getString(final String name) {
			return values.get(name)[0];
		}
		
		public int getInt(final String name) {
			return Integer.parseInt(values.get(name)[0]);
		}
	}
	
	public static class JobParams {
		final String jobName;
		final String url;
		final JobParam[] input;
		
		private JobParams(
			final String jobName, 
			final String url, 
			final JobParam... params
		) {
			this.jobName = jobName;
			this.url = url;
			this.input = params;
		}
	}
	
	public static class JobParam {
		final String name;
		
		private JobParam(final String name) {
			this.name = name;
		}
	}
	
	public static class InputParam extends JobParam {
		boolean isText = true;
		
		private InputParam(final String name) {
			super(name);
		}
	}
	
	public static class SelectParam extends JobParam {
		final boolean isSelect = true;
		final String[] values;
		
		private SelectParam(
			final String name, 
			final String[] values
		) {
			super(name);
			this.values = values;
		}
	}
	
	public final InputParam input(
		final String name
	) {
		return new InputParam(name);
	}
	
	public final SelectParam select(
		final String name, 
		final String... values
	) {
		return new SelectParam(name, values);
	}
	
	public final JobParams params(
		final String jobName,
		final String url,
		final JobParam... params
	) {
		return new JobParams(jobName, url, params);
	}

	@Inject DefaultMustacheFactory mf;
	private Mustache template;
	private JobParams jobParams;
	
	@Override
	public void init() throws ServletException {
		super.init();
		this.template = mf.compile(new InputStreamReader(getServletContext().getResourceAsStream("/WEB-INF/templates/pipeline-job.html")), "pipeline-job");	
		this.jobParams = createJobParamsDefinition();
	}
	
	@Override
	public final void doGet(
		final HttpServletRequest req, 
		final HttpServletResponse resp
	) throws 
		IOException 
	{
		try {
			if ("true".equals(req.getParameter("runjob"))) {
				final PipelineService service = 
						PipelineServiceFactory.newPipelineService();
				
				final String pipelineId = 
					startJob(service, new JobParamValues(req.getParameterMap()));
	
				redirectToPipelineStatus(resp, pipelineId);
				
			} else {
	
			    template.execute(resp.getWriter(), jobParams);
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void redirectToPipelineStatus(
		final HttpServletResponse resp,
		final String pipelineId
	) throws 
		IOException 
	{
		String destinationUrl = "/_ah/pipeline/status.html?root=" + pipelineId;
		resp.sendRedirect(destinationUrl);
	}

}
