package com.stresster.resources.reports;

import java.util.List;

import com.stresster.resources.Response;

public class ApiResponseDetail
{
	private String apiUri;
	private List<Response> responses;

	public ApiResponseDetail(String apiUri, List<Response> responses)
	{
		this.apiUri = apiUri;
		this.responses = responses;
	}

	public String getApiUri()
	{
		return apiUri;
	}

	public List<Response> getResponses()
	{
		return responses;
	}
}
