package com.stresster.resources.reports;

public class Iteration
{
	private int iterationNumber;
	private long apiCalls;
	private String executionTime;

	public Iteration(int iterationNumber, long apiCalls, String executionTime)
	{
		this.iterationNumber = iterationNumber;
		this.apiCalls = apiCalls;
		this.executionTime = executionTime;
	}

	public int getIterationNumber()
	{
		return iterationNumber;
	}

	public long getApiCalls()
	{
		return apiCalls;
	}

	public String getExecutionTime()
	{
		return executionTime;
	}
}
