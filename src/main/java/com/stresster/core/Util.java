package com.stresster.core;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class Util
{
	public static String getPropsFilePathFromCommandLine(String pattern, String... args)
	{
		for(String arg : args)
		{
			if(arg.startsWith(pattern))
			{
				return arg.substring(pattern.length());
			}
		}

		return "";
	}

	public static void softKillExecutor(ExecutorService executorService)
	{
		try
		{
			if(!executorService.awaitTermination(60, TimeUnit.SECONDS))
			{
				executorService.shutdownNow();
			}
		}
		catch(InterruptedException e)
		{
			executorService.shutdownNow();
		}
	}

	public static final class HttpClient
	{
		public static final Set<String> RESTRICTED_HEADERS = Set.of(
			"connection", "content-length", "expect", "host", "upgrade",
			"accept-encoding", "content-encoding", "transfer-encoding"
		);

		static String[] convertHeaders(JSONObject headers)
		{
			return headers.keySet()
				.stream()
				.filter(Predicate.not(RESTRICTED_HEADERS::contains))
				.flatMap(key -> Stream.of(key, headers.getString(key)))
				.toArray(String[]::new);
		}

		static String paramsToQueryString(JSONObject params)
		{
			return params.keySet()
				.stream()
				.map(key -> key + "=" + params.get(key).toString())
				.collect(Collectors.joining("&"));
		}

		public static String parseErrorDataFromResponse(JSONObject response)
		{
			try
			{
				return response.getString("message");
			}
			catch(Exception ex)
			{
				return StringUtils.EMPTY;
			}
		}
	}
}
