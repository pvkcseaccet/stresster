package com.stresster.resources;

import lombok.Builder;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@Builder(builderMethodName = "newBuilder")
public class TestResults
{
	private Long executionTime;
	private List<Response> responseList;
	public static final Long INVALID_EXECUTION_TIME = -1L;

	public static TestResults empty()
	{
		return new Empty(Arrays.asList());
	}

	public static final class Empty extends TestResults
	{
		Empty(List<Response> responseList)
		{
			super(INVALID_EXECUTION_TIME, responseList);
		}
	}
}
