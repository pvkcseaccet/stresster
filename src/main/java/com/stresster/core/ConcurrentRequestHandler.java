package com.stresster.core;

import static com.stresster.core.Util.HttpClient.RESTRICTED_HEADERS;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.stresster.resources.ConcurrentRequest;
import com.stresster.resources.Request;
import com.stresster.resources.Response;
import com.stresster.resources.TestResults;

public class ConcurrentRequestHandler
{

	private static final Logger LOGGER = Logger.getLogger(ConcurrentRequestHandler.class.getName());

	public static TestResults doWork(ConcurrentRequest concurrentRequest, ExecutorService executorService, int iterations)
	{
		long timeInMillis = System.currentTimeMillis();
		try
		{
			List<Response> responseList = doWork0(concurrentRequest, executorService, iterations);
			return TestResults.newBuilder()
				.executionTime(System.currentTimeMillis() - timeInMillis)
				.responseList(responseList)
				.build();
		}
		catch(Exception ex)
		{
			LOGGER.severe("" + ex);
		}
		return TestResults.empty();
	}

	private static List<Response> doWork0(ConcurrentRequest concurrentRequest, ExecutorService executorService, int iterations)
	{

		List<Request> requestList = concurrentRequest.getHttpRequestList();

		if(requestList == null)
		{
			LOGGER.severe("Nothing to test for now.");
			return null;
		}

		List<FutureTask<Response>> futureTasks = Collections.nCopies(iterations, requestList)
			.stream()
			.flatMap(Collection::stream)
			.map((request) -> getTask.apply(concurrentRequest, request))
			.collect(Collectors.toList());

		futureTasks.forEach(executorService::execute);

		return futureTasks.stream().map(x -> {
			try
			{
				return x.get();
			}
			catch(Exception ex)
			{
				return null;
			}
		}).collect(Collectors.toList());

	}

	private static BiFunction<ConcurrentRequest, Request, FutureTask<Response>> getTask = (concurrentRequest, request) -> new FutureTask<Response>(() -> {
		try
		{

			URL url = new URL(concurrentRequest.getTestDomain() + request.getUri());
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod(request.getMethod().toUpperCase());

			JSONObject headers = new JSONObject(request.getHeaders());
			for(String key : headers.keySet())
			{
				if(!RESTRICTED_HEADERS.contains(key.toLowerCase()))
				{
					connection.setRequestProperty(key, headers.getString(key));
				}
			}
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36");

			String queryParams = Util.HttpClient.paramsToQueryString(new JSONObject(request.getQueryParams()));
			String bodyContent = Optional.ofNullable(request.getRequestBody())
				.map(body -> queryParams + "&JSONString=" + URLEncoder.encode(body, StandardCharsets.UTF_8))
				.orElse(queryParams);

			concurrentRequest.getBarrier().await();

			if(!bodyContent.isEmpty())
			{
				connection.setDoOutput(true);
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				try(OutputStream os = connection.getOutputStream())
				{
					byte[] input = bodyContent.getBytes(StandardCharsets.UTF_8);
					os.write(input, 0, input.length);
				}
			}

			int statusCode = connection.getResponseCode();
			String responseBody = StringUtils.EMPTY;
			try(BufferedReader br = new BufferedReader(
				new InputStreamReader(
					statusCode < 400 ? connection.getInputStream() :
						connection.getErrorStream(),
					StandardCharsets.UTF_8)))
			{
				responseBody = br.lines().collect(Collectors.joining("\n"));
			}

			connection.disconnect();

			return Response.newBuilder()
				.statusCode(statusCode)
				.responseBody(responseBody)
				.requestURI(request.getUri())
				.build();

		}
		catch(Exception e)
		{
			LOGGER.severe("Error processing request: " + e);
		}

		return Response.empty();
	});
}

