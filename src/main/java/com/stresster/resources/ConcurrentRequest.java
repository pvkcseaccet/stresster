package com.stresster.resources;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.CyclicBarrier;

import com.stresster.domain.TestType;

@Getter
@Builder(builderClassName = "Builder")
@Setter
public class ConcurrentRequest
{
	private int testID;
	private TestType testType;
	private String reportFilePath;
	private String testDomain;
	private int noOfThreads;
	private CyclicBarrier barrier;
	private List<Request> httpRequestList;

	public int setTestID(int testID)
	{
		this.testID = testID;
		return testID;
	}
}
