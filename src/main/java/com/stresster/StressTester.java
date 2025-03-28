package com.stresster;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.stresster.core.ConcurrentRequestHandler;
import com.stresster.core.Util;
import com.stresster.domain.TestType;
import com.stresster.exception.StressterExceptionStore;
import com.stresster.parser.DataFetcher;
import com.stresster.reports.ReportGenerator;
import com.stresster.resources.ConcurrentRequest;
import com.stresster.resources.TestProps;

public class StressTester
{

	private static final Logger LOGGER = Logger.getLogger(StressTester.class.getName());

	public static void main(String[] args) throws Exception
	{
		String propsFilePath = Util.getPropsFilePathFromCommandLine(args);
		if ("".equalsIgnoreCase(propsFilePath))
		{
			throw StressterExceptionStore.INVALID_ARGUMENT_RECEIVED_FROM_CLIENT._new();
		}
		doTest(propsFilePath);
	}

	public static void doTest(String propsFilePath) throws Exception
	{
		LOGGER.info("Executing StressTer...");

		TestProps props = DataFetcher.getProps(propsFilePath);

		ReportGenerator reportGenerator = new ReportGenerator();
		for(int i = 1; i <= props.getIterations(); i++)
		{
			int noOfAPICalls = (int) Math.pow(2, i);
			ConcurrentRequest request = ConcurrentRequest.builder()
				.testType(props.getTestType())
				.noOfThreads(noOfAPICalls)
				.testDomain(props.getDomain())
				.barrier(new CyclicBarrier(noOfAPICalls))
				.httpRequestList(DataFetcher.get(props.getTestDataFilePath())
					.entrySet()
					.stream()
					.filter(x -> props.getURLsToTest().contains(x.getKey()))
					.map(Map.Entry::getValue)
					.flatMap(Collection::stream)
					.collect(Collectors.toList()))
				.build();

			if (request.getHttpRequestList() != null && request.getHttpRequestList().isEmpty())
			{
				throw StressterExceptionStore.REQUEST_LIST_EMPTY._new();
			}

			ExecutorService executorService = Executors.newFixedThreadPool(noOfAPICalls * request.getHttpRequestList().size());
			Long responseTime = ConcurrentRequestHandler.doWork(request, executorService, noOfAPICalls);
			executorService.shutdown();
			Util.softKillExecutor(executorService);
			reportGenerator.doAccumulateReport(i, responseTime);
		}

		reportGenerator.printReport();
		reportGenerator.publishReport(props.getDomain(), String.join(",", props.getURLsToTest()), TestType.EXPONENTIAL);
	}

}