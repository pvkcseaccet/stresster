package com.stresster.domain;

import lombok.Getter;

@Getter
public enum TestType
{
	LINEAR("Linear", 1),
	INCREMENTAL("Incremental", 2),
	EXPONENTIAL("Exponential", 2);

	private final String type;
	private final int incrementBy;

	TestType(String type, int incrementBy)
	{
		this.type = "Concurrent + " + type;;
		this.incrementBy = incrementBy;
	}
}
