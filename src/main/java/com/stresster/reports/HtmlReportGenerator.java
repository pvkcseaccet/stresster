package com.stresster.reports;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
		context.setVariable("totalApiCalls", (long) reportsContext.getTestedAPIs().size()  * ((int) Math.pow(2, reportsContext.getReports().size() + 1) - 2));
		context.setVariable("totalExecutionTime", reportsContext.getReports().values().stream().mapToLong(Long::longValue).sum() + "s");

		List<Iteration> iterations = new ArrayList<>();
		for(var entry : reportsContext.getReports().entrySet())
		{
			List<ApiResponseDetail> apiResponseDetails = new ArrayList<>();
			for(var apiEntry : reportsContext.getApiResponses().get(entry.getKey()).entrySet())
			{
				apiResponseDetails.add(new ApiResponseDetail(apiEntry.getKey(), apiEntry.getValue()
					.stream()
					.map(x -> {
						if (StringUtils.isEmpty(x.getResponseBody()))
						{
							x.setResponseBody("StressTer:: Response Not Saved.");
							return x;
						}
						return x;
					})
					.collect(Collectors.toList())));
			}

			iterations.add(new Iteration(entry.getKey(), (long) reportsContext.getTestedAPIs().size() * (int) Math.pow(2, entry.getKey()), reportsContext.getReports().get(entry.getKey()) + "s", apiResponseDetails));
		}
		context.setVariable("iterations", iterations);

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
