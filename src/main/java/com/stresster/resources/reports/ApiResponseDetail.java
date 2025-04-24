package com.stresster.resources.reports;

import lombok.Data;

import java.util.List;

import com.stresster.resources.Response;

@Data
public class ApiResponseDetail
{
	private String apiUri;
	private List<Response> responses;
	private Long succeededResponses;
	private Long failedResponses;
	private Long executionTime;

	public ApiResponseDetail(String apiUri, List<Response> responses, Long succeededResponses, Long failedResponses, Long executionTime)
	{
		this.apiUri = apiUri;
		this.responses = responses;
		this.succeededResponses = succeededResponses;
		this.failedResponses = failedResponses;
		this.executionTime = executionTime / 1000;
	}

}
