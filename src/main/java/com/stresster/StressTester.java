package com.stresster;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.stresster.core.ConcurrentRequestHandler;
import com.stresster.core.Util;
import com.stresster.exception.StressterExceptionStore;
import com.stresster.parser.DataFetcher;
import com.stresster.reports.ReportingDataAccumulator;
import com.stresster.resources.ConcurrentRequest;
import com.stresster.resources.TestProps;
import com.stresster.resources.TestResults;
import com.stresster.resources.reports.ReportsContext;

public class StressTester
{

	private static final Logger LOGGER = Logger.getLogger(StressTester.class.getName());

	public static void main(String[] args) throws Exception
	{
		String propsFilePath = Util.getPropsFilePathFromCommandLine("--test-props=", args);
		boolean needAPIResponse = Boolean.parseBoolean(Util.getPropsFilePathFromCommandLine("--need-api-response=", args));
		if ("".equalsIgnoreCase(propsFilePath))
		{
			throw StressterExceptionStore.INVALID_ARGUMENT_RECEIVED_FROM_CLIENT._new();
		}
		doTest(propsFilePath, needAPIResponse);
	}

	public static void doTest(String propsFilePath, boolean needAPIResponse) throws Exception
	{
		LOGGER.info("Executing StressTer...");

		TestProps props = DataFetcher.getProps(propsFilePath);

		ReportingDataAccumulator reportingDataAccumulator = new ReportingDataAccumulator();
		for(int i = 1; i <= props.getIterations(); i++)
		{
			int noOfAPICalls = (int) Math.pow(2, i);
			ConcurrentRequest request = ConcurrentRequest.builder()
				.testType(props.getTestType())
				.testDomain(props.getDomain())
				.barrier(new CyclicBarrier(noOfAPICalls))
				.httpRequestList(DataFetcher.get(props.getTestDataFilePath())
					.entrySet()
					.stream()
					.filter(x -> props.getURLsToTest().stream().anyMatch(y -> x.getKey().matches(y)))
					.map(Map.Entry::getValue)
					.flatMap(Collection::stream)
					.collect(Collectors.toList()))
				.build();

			int noOfThreads = noOfAPICalls * request.getHttpRequestList().size();
			request.setNoOfThreads(noOfThreads);

			if (request.getHttpRequestList() != null && request.getHttpRequestList().isEmpty())
			{
				throw StressterExceptionStore.REQUEST_LIST_EMPTY._new();
			}

			ExecutorService executorService = Executors.newFixedThreadPool(noOfThreads);
			TestResults results = ConcurrentRequestHandler.doWork(request, executorService, noOfAPICalls);
			if (TestResults.INVALID_EXECUTION_TIME.equals(results.getExecutionTime()))
			{
				throw StressterExceptionStore.TEST_EXECUTION_FAILED._new();
			}
			executorService.shutdown();
			Util.softKillExecutor(executorService);
			reportingDataAccumulator.doAccumulateReport(i, results);
		}

		String fileNamePrefix = reportingDataAccumulator.getReportsFolder() + "Report-" + System.currentTimeMillis();
		ReportsContext context = ReportsContext.builder()
			.reports(reportingDataAccumulator.getReports())
			.apiResponses(needAPIResponse? reportingDataAccumulator.getApiResponses() : new HashMap<>())
			.testedDomain(props.getDomain())
			.testedAPIs(new ArrayList<>(props.getURLsToTest()))
			.testMode(props.getTestType().getType())
			.htmlReportFile(new File(fileNamePrefix + ".html"))
			.build();
		reportingDataAccumulator.publishReport(context);
	}

}