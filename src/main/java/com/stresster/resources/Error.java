package com.stresster.resources;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder()
@Getter
public class Error extends Response
{
	private String errorData;

	Error(int statusCode, String responseBody, String requestURI, long timeTakeninMillis)
	{
		super(statusCode, responseBody, requestURI, timeTakeninMillis);
	}

	Error()
	{
		super();
	}
}
