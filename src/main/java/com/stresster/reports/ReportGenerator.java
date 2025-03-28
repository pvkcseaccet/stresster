package com.stresster.reports;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;

public class ReportGenerator
{

	private static final Logger LOGGER = Logger.getLogger(ReportGenerator.class.getName());

	private Map<Integer, Long> reports;
	private String reportsFolder;

	public ReportGenerator()
	{
		this.reports = new HashMap<>();
		this.reportsFolder = "/Users/vijay-5428/POCs/stresster/data/reports/";
		File f = new File(reportsFolder);
		if (!f.exists())
		{
			f.mkdir();
		}
	}

	public void doAccumulateReport(int iteration, Long time)
	{
		reports.put(iteration, time);
	}

	public void printReport()
	{
		reports.
			forEach((key1, value1) ->
				LOGGER.info("Iteration : " + key1+ " took " + value1/1000 + "s to execute."));
	}

	public void publishReport(String domainTested, String apiToTest, com.stresster.domain.TestType testType) throws Exception
	{
		File reportsFile = new File(this.reportsFolder + "Report-" + System.currentTimeMillis()+".txt");
		String report = "";
		report += "Tested Domain: " + domainTested + "\n";
		report += "Tested API: " + apiToTest + "\n";
		report += "Method: POST \n";
		report += "Test Mode: " + testType.getType() + "\n";
		report += "\n=============================================================================\n";
		report += "Total Iterations Done: " + reports.size() + "\n";
		report += "Total API Calls Triggered: " + IntStream.range(1, reports.size()+1)
			.boxed()
			.map(x ->(int) Math.pow(2, x))
			.reduce(Integer::sum)
			.get() + "\n";
		report += "Total Time Spent: " + reports.values().stream().reduce(Long::sum).get()/1000 + "s";
		report += "\n=============================================================================\n\n";
		AtomicInteger i = new AtomicInteger(1);
		report += reports.entrySet()
			.stream()
			.map((entry) -> "Iteration : " + entry.getKey()+ " with " + (int) Math.pow(2, i.getAndIncrement()) + " API Calls " + " took " + entry.getValue()/1000 + "s to execute")
			.collect(Collectors.joining("\n"));
		report += "\n=============================================================================\n";
		FileUtils.writeStringToFile(reportsFile,report, StandardCharsets.UTF_8);
		LOGGER.info("Test Report published at " + reportsFile.getPath());
	}
}
