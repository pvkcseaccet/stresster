package com.stresster.resources;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(builderMethodName = "newBuilder")
public class Response
{
	private int statusCode;
	private String responseBody;
	private String requestURI;

	public static Response empty()
	{
		return new Empty();
	}

	public static final class Empty extends Response
	{
		private Response response;
		private Empty()
		{
			super(-1, "", "");
		}

	}
}
