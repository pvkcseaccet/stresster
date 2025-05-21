package com.stresster.reports;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.stresster.exception.StressterException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.stresster.core.Util;
import com.stresster.exception.StressterExceptionStore;
import com.stresster.resources.Error;
import com.stresster.resources.Response;
import com.stresster.resources.reports.ApiResponseDetail;
import com.stresster.resources.reports.Iteration;
import com.stresster.resources.reports.ReportsContext;

public class HtmlReportGenerator
{

	public static final Predicate<Integer> SUCCEEDED_REQUESTS = x -> x > 199 && x < 299;


	public void generateReport(ReportsContext reportsContext) throws StressterException
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

		Map<Boolean, Long> totalSucceededAndOtherCalls = reportsContext.getApiResponses()
			.values()
			.stream()
			.map(Map::values)
			.flatMap(Collection::stream)
			.flatMap(List::stream)
			.map(Response::getStatusCode)
			.collect(Collectors.partitioningBy(SUCCEEDED_REQUESTS, Collectors.counting()));

		context.setVariable("totalApiCallsSucceeded", totalSucceededAndOtherCalls.get(true));
		context.setVariable("totalApiCallsFailed", totalSucceededAndOtherCalls.get(false));

		List<Iteration> iterations = new ArrayList<>();
		Map<String, List<Long>> uriVsIterationMaxTime = new HashMap<>();
		Map<String, Map<String, Long>> uriVsExceptionCount = new HashMap<>();
		for(var entry : reportsContext.getReports().entrySet())
		{
			List<ApiResponseDetail> apiResponseDetails = new ArrayList<>();
			for(var apiEntry : reportsContext.getApiResponses().get(entry.getKey()).entrySet())
			{
				Map<Boolean, Long> succeededVsFailedCounts = apiEntry.getValue()
					.stream()
					.map(Response::getStatusCode)
					.collect(Collectors.partitioningBy(SUCCEEDED_REQUESTS, Collectors.counting()));

				uriVsExceptionCount.computeIfAbsent(apiEntry.getKey(), x -> new HashMap<>())
					.putAll(apiEntry.getValue()
						.stream()
						.flatMap(x -> x instanceof Error ? Stream.of((Error) x) : Stream.empty())
						.filter(Objects::nonNull)
						.collect(Collectors.groupingBy(Error::getErrorData, Collectors.counting())));

				uriVsExceptionCount.computeIfAbsent(apiEntry.getKey(), x -> new HashMap<>())
					.putAll(apiEntry.getValue()
						.stream()
						.filter(x -> !SUCCEEDED_REQUESTS.test(x.getStatusCode()))
						.map(Response::getResponseBody)
						.flatMap(x -> StringUtils.startsWith(x, "{") ? Stream.of(Util.HttpClient.parseErrorDataFromResponse(new JSONObject(StringUtils.trim(x)))) : Stream.empty())
						.filter(Objects::nonNull)
						.collect(Collectors.groupingBy(Function.identity(), Collectors.counting())));

				apiResponseDetails.add(new ApiResponseDetail(apiEntry.getKey(), apiEntry.getValue()
					.stream()
					.map(x -> {
						if (StringUtils.isEmpty(x.getResponseBody()))
						{
							x.setResponseBody("StressTer:: Response Not Saved.");
							return x;
						}
						return x;
					}).collect(Collectors.toList()),
					succeededVsFailedCounts.get(true),
					succeededVsFailedCounts.get(false),
					apiEntry.getValue()
						.stream()
						.map(Response::getTimeTakeninMillis)
						.max(Long::compareTo)
						.orElse(-1L)));

				uriVsIterationMaxTime.computeIfAbsent(apiEntry.getKey(), x -> new ArrayList<>()).add(apiEntry.getValue()
					.stream()
					.map(Response::getTimeTakeninMillis)
					.max(Long::compareTo)
					.orElse(-1L));
			}

			iterations.add(new Iteration(entry.getKey(), (long) reportsContext.getTestedAPIs().size() * (int) Math.pow(2, entry.getKey()), reportsContext.getReports().get(entry.getKey()) + "s", apiResponseDetails));
		}
		context.setVariable("iterationsForBar", new ArrayList<>(iterations));
		context.setVariable("iterations", iterations);
		context.setVariable("uriVsIterations", uriVsIterationMaxTime);
		Map<String, List<Map.Entry<String, Long>>> flattened = new LinkedHashMap<>();
		for (Map.Entry<String, Map<String, Long>> outer : uriVsExceptionCount.entrySet()) {
			flattened.put(outer.getKey(), new ArrayList<>(outer.getValue().entrySet()));
		}
		context.setVariable("exceptionStats", flattened);

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
