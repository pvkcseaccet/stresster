package com.stresster.reports;

import com.stresster.exception.StressterException;
import lombok.Getter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.json.JSONObject;

import com.stresster.resources.Response;
import com.stresster.resources.TestResults;
import com.stresster.resources.reports.ReportsContext;

@Getter
public class ReportingDataAccumulator
{

	private static final Logger LOGGER = Logger.getLogger(ReportingDataAccumulator.class.getName());

	private Map<Integer, Long> reports;
	private Map<Integer, Map<String, List<Response>>> apiResponses;
	private String reportsFolder;

	public ReportingDataAccumulator()
	{
		this.reports = new HashMap<>();
		this.apiResponses = new HashMap<>();
		this.reportsFolder = "/Users/vijay-5428/POCs/stresster/data/reports/";
		File f = new File(reportsFolder);
		if (!f.exists())
		{
			f.mkdir();
		}
	}

	public void doAccumulateReport(int iteration, TestResults results)
	{
		reports.put(iteration, results.getExecutionTime());
		this.apiResponses.put(iteration, new HashMap<>());
		this.apiResponses.get(iteration)
			.putAll(results.getResponseList()
				.stream()
				.collect(
					Collectors.groupingBy(Response::getRequestURI)
				)
			);
	}

	public void printReport()
	{
		reports.
			forEach((key1, value1) ->
				LOGGER.info("Iteration : " + key1+ " took " + value1/1000 + "s to execute."));

		LOGGER.info(getFormattedAPIResponse());
	}

	public String getFormattedAPIResponse()
	{
		return "APIResponse : " + apiResponses.entrySet()
			.stream()
			.map((e) ->
				"Iteration : #" + e.getKey() + e.getValue()
					.entrySet()
					.stream()
					.map((ee) -> "\n=============================================================================\n" +
						"URI: "+ ee.getKey() + "\n================\n" + ee.getValue()
						.stream()
						.map(Response::getResponseBody)
						.map(JSONObject::new)
						.map(JSONObject::toString)
						.reduce((x, y) -> x + "\n================\n" + y))
					.reduce((x, y) -> x + "\n=============================================================================\n" + y).get())
			.reduce((x, y) -> x + "\n=============================================================================\n" + y).get();
	}

	public void publishReport(ReportsContext context) throws StressterException
	{
		HtmlReportGenerator reportGenerator = new HtmlReportGenerator();
		reportGenerator.generateReport(context);
	}
}
