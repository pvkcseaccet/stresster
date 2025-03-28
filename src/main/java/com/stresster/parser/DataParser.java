package com.stresster.parser;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.stresster.resources.Request;
import com.stresster.resources.TestProps;

public class DataParser
{
	private static final Function<JSONObject, Request> PARSE_REQUEST = (requestJSON) ->
		Request.builder()
			.url(requestJSON.getJSONObject("data").optString("url", ""))
			.uri(requestJSON.getJSONObject("data").optString("uri", ""))
			.cookie(requestJSON.getJSONObject("data").optString("cookies", ""))
			.headers(requestJSON.getJSONObject("data").optJSONObject("request_headers", new JSONObject()))
			.method(requestJSON.getJSONObject("data").optString("http_method", ""))
			.queryParams(requestJSON.getJSONObject("data").optJSONObject("params", new JSONObject()))
			.requestBody(new JSONObject(requestJSON.getJSONObject("data").optString("request_data", new JSONObject().toString())).toString())
			.build();

	static List<Request> getParsedRequestList(String jsonContent)
	{
		JSONArray requestJSONArray = new JSONArray(jsonContent);
		return requestJSONArray.toList()
			.stream()
			.map(x -> new JSONObject((HashMap)x))
			.map(PARSE_REQUEST)
			.collect(Collectors.toList());
	}

	static TestProps getParsedTestProps(String fileContent)
	{
		return new Gson().fromJson(fileContent, TestProps.class);
	}
}
