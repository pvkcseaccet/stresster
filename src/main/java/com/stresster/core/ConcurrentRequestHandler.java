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
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

import com.stresster.resources.ConcurrentRequest;
import com.stresster.resources.Request;

public class ConcurrentRequestHandler
{

	private static final Logger LOGGER = Logger.getLogger(ConcurrentRequestHandler.class.getName());

	public static Long doWork(ConcurrentRequest concurrentRequest, ExecutorService executorService, int iterations)
	{
		long timeInMillis = System.currentTimeMillis();
		try
		{
			doWork0(concurrentRequest, executorService, iterations);
		}
		catch(Exception ex)
		{
			LOGGER.severe("" + ex);
		}
		return System.currentTimeMillis() - timeInMillis;
	}

	private static void doWork0(ConcurrentRequest concurrentRequest, ExecutorService executorService, int iterations)
	{

		List<Request> requestList = concurrentRequest.getHttpRequestList();

		if(requestList == null)
		{
			LOGGER.severe("Nothing to test for now.");
			return;
		}

		List<Integer> response;

		List<FutureTask<Integer>> futureTasks = Collections.nCopies(iterations, requestList)
			.stream()
			.flatMap(Collection::stream)
			.map((request) -> getTask.apply(concurrentRequest, request))
			.collect(Collectors.toList());

		futureTasks.forEach(executorService::execute);

		response = futureTasks.stream().map(x -> {
			try
			{
				return x.get();
			}
			catch(Exception ex)
			{
				return -1;
			}
		}).collect(Collectors.toList());

	}

	private static BiFunction<ConcurrentRequest, Request, FutureTask<Integer>> getTask = (concurrentRequest, request) -> new FutureTask<>(() -> {
		try
		{

			URL url = new URL(concurrentRequest.getTestDomain() + request.getUri());
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod(request.getMethod().toUpperCase());

			JSONObject headers = request.getHeaders();
			for(String key : headers.keySet())
			{
				if(!RESTRICTED_HEADERS.contains(key.toLowerCase()))
				{
					connection.setRequestProperty(key, headers.getString(key));
				}
			}
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36");

			String queryParams = Util.HttpClient.paramsToQueryString(request.getQueryParams());
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
			String responseBody;
			try(BufferedReader br = new BufferedReader(
				new InputStreamReader(
					statusCode < 400 ? connection.getInputStream() :
						connection.getErrorStream(),
					StandardCharsets.UTF_8)))
			{
				responseBody = br.lines().collect(Collectors.joining("\n"));
			}

			connection.disconnect();

			String result = "Response for " + request.getUri() + ": " + responseBody;
			return statusCode;

		}
		catch(Exception e)
		{
			LOGGER.severe("Error processing request: " + e);
		}

		return -1;
	});
}

