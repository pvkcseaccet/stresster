package com.stresster.core;

import static com.stresster.core.Util.HttpClient.RESTRICTED_HEADERS;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.stresster.reports.HtmlReportGenerator;
import com.stresster.resources.ConcurrentRequest;
import com.stresster.resources.Error;
import com.stresster.resources.Request;
import com.stresster.resources.Response;
import com.stresster.resources.TestResults;

public class ConcurrentRequestHandler
{

	private static final Logger LOGGER = Logger.getLogger(ConcurrentRequestHandler.class.getName());
	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
		.followRedirects(HttpClient.Redirect.NORMAL)
		.build();

	public static TestResults doWork(ConcurrentRequest concurrentRequest, int iterations)
	{
		long timeInMillis = System.currentTimeMillis();
		try
		{
			List<Response> responseList = doWork0(concurrentRequest, iterations);
			return TestResults.newBuilder()
				.executionTime((System.currentTimeMillis() - timeInMillis) / 1000)
				.responseList(responseList)
				.build();
		}
		catch(Exception ex)
		{
			LOGGER.severe("" + ex);
		}
		return TestResults.empty();
	}

	private static List<Response> doWork0(ConcurrentRequest concurrentRequest, int iterations) throws InterruptedException
	{

		List<Request> requestList = concurrentRequest.getHttpRequestList();

		if(requestList == null)
		{
			LOGGER.severe("Nothing to test for now.");
			return null;
		}

		ExecutorService service = Executors.newCachedThreadPool();
		try
		{
			List<Future<Response>> calls = Collections.nCopies(iterations, requestList)
				.parallelStream()
				.flatMap(Collection::stream)
				.map((request) -> getTask.apply(concurrentRequest, request))
				.map(service::submit)
				.collect(Collectors.toList());

			return calls.stream()
				.map(responseFutureTask -> {
					try
					{
						return responseFutureTask.get();
					}
					catch(InterruptedException | ExecutionException e)
					{
						throw new RuntimeException(e);
					}
				})
				.collect(Collectors.toList());
		}
		finally
		{
			service.shutdown();
			if (!service.awaitTermination(60, TimeUnit.SECONDS))
			{
				service.shutdownNow();
			}
		}
	}

	private static BiFunction<ConcurrentRequest, Request, Callable<Response>> getTask = (concurrentRequest, request) -> (() -> {
		Long startTime = System.currentTimeMillis();
		try
		{

			String queryParams = Util.HttpClient.paramsToQueryString(new JSONObject(request.getQueryParams()));
			queryParams += "&" + getEncodedFormData(request, queryParams);
			URI uri = URI.create(concurrentRequest.getTestDomain() + request.getUri() + "?" + queryParams);

			HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
				.uri(uri)
				.method(request.getMethod().toUpperCase(), getBodyPublisher(request, queryParams));

			JSONObject headers = new JSONObject(request.getHeaders());
			for(String key : headers.keySet())
			{
				if(!RESTRICTED_HEADERS.contains(key.toLowerCase()))
				{
					requestBuilder.header(key, headers.getString(key));
				}
			}
			requestBuilder.header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36");

			concurrentRequest.getBarrier().await();

			HttpRequest httpRequest = requestBuilder.build();
			HttpResponse<String> response = HTTP_CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

			int statusCode = response.statusCode();
			String responseBody = response.body();
			return Response.newBuilder()
				.statusCode(statusCode)
				.responseBody(!concurrentRequest.isSaveResponse() && HtmlReportGenerator.SUCCEEDED_REQUESTS.test(statusCode) ? "StressTer:: Response Not Saved" : responseBody)
				.requestURI(request.getUri())
				.timeTakeninMillis(System.currentTimeMillis() - startTime)
				.build();

		}
		catch(Exception e)
		{
			LOGGER.severe("Error processing request: " + e);
			return Error.newBuilder()
				.statusCode(-1)
				.responseBody(e.getMessage())
				.requestURI(request.getUri())
				.timeTakeninMillis(System.currentTimeMillis() - startTime)
				.build();
		}
	});

	private static HttpRequest.BodyPublisher getBodyPublisher(Request request, String queryParams)
	{
		String bodyContent = "";
		if(StringUtils.isNotEmpty(request.getFormDataParamName()))
		{
			bodyContent = URLEncoder.encode(Optional.ofNullable(request.getRequestBody()).orElse(queryParams), StandardCharsets.UTF_8);
		}

		bodyContent = (StringUtils.isNotEmpty(request.getFormDataParamName())
			? request.getFormDataParamName() + "=" : StringUtils.EMPTY) + bodyContent;

		if(StringUtils.isNotEmpty(bodyContent))
		{
			return HttpRequest.BodyPublishers.ofString(bodyContent, StandardCharsets.UTF_8);
		}
		return HttpRequest.BodyPublishers.noBody();
	}

	private static String getEncodedFormData(Request request, String queryParams)
	{
		String bodyContent = "";
		if(StringUtils.isNotEmpty(request.getFormDataParamName()))
		{
			bodyContent = URLEncoder.encode(Optional.ofNullable(request.getRequestBody()).orElse(queryParams), StandardCharsets.UTF_8);
		}

		return (StringUtils.isNotEmpty(request.getFormDataParamName())
			? request.getFormDataParamName() + "=" : StringUtils.EMPTY) + bodyContent;

	}
}

