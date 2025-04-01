package com.stresster.resources.reports;

import lombok.Getter;

import java.util.List;

@Getter
public class Iteration
{
	private int iterationNumber;
	private long apiCalls;
	private String executionTime;
	private List<ApiResponseDetail> apiResponseDetail;

	public Iteration(int iterationNumber, long apiCalls, String executionTime, List<ApiResponseDetail> apiResponseDetail)
	{
		this.iterationNumber = iterationNumber;
		this.apiCalls = apiCalls;
		this.executionTime = executionTime;
		this.apiResponseDetail = apiResponseDetail;
	}

}
