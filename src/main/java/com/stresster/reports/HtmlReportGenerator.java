package com.stresster.reports;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.stresster.exception.StressterExceptionStore;
import com.stresster.resources.reports.ApiResponseDetail;
import com.stresster.resources.reports.Iteration;
import com.stresster.resources.reports.ReportsContext;

public class HtmlReportGenerator
{

	public void generateReport(ReportsContext reportsContext)
	{
		TemplateEngine templateEngine = new TemplateEngine();
		ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
		resolver.setPrefix("/templates/");
		resolver.setSuffix(".html");
		resolver.setTemplateMode("HTML");
		templateEngine.setTemplateResolver(resolver);

		Context context = new Context();
		context.setVariable("testedDomain", reportsContext.getTestedDomain());
		context.setVariable("testedApis", reportsContext.getTestedAPIs());
		context.setVariable("testMode", reportsContext.getTestMode());

		context.setVariable("totalIterations", reportsContext.getReports().size());
		context.setVariable("totalApiCalls", reportsContext.getReports().values().stream().mapToLong(Long::longValue).sum());
		context.setVariable("totalExecutionTime", reportsContext.getReports().values().stream().mapToLong(Long::longValue).sum() + "s");

		List<Iteration> iterations = new ArrayList<>();
		for(var entry : reportsContext.getReports().entrySet())
		{
			iterations.add(new Iteration(entry.getKey(), entry.getValue(), reportsContext.getReports().get(entry.getKey()) + "s"));
		}
		context.setVariable("iterations", iterations);

		List<ApiResponseDetail> responseDetails = new ArrayList<>();
		for(var iterationEntry : reportsContext.getApiResponses().entrySet())
		{
			for(var apiEntry : iterationEntry.getValue().entrySet())
			{
				responseDetails.add(new ApiResponseDetail(apiEntry.getKey(), apiEntry.getValue()));
			}
		}
		context.setVariable("apiResponses", responseDetails);

		String output = templateEngine.process("report", context);

		try
		{
			FileUtils.writeStringToFile(reportsContext.getHtmlReportFile(), output, StandardCharsets.UTF_8);
		}
		catch(Exception e)
		{
			throw StressterExceptionStore.REPORT_GENERATION_FAILED._new(e);
		}
	}
}
