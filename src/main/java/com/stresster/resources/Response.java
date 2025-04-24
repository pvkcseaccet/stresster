package com.stresster.resources;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(builderMethodName = "newBuilder")
@AllArgsConstructor
@NoArgsConstructor
public class Response
{
	private int statusCode;
	private String responseBody;
	private String requestURI;
	private long timeTakeninMillis;

	public static Response empty()
	{
		return new Empty();
	}

	public static final class Empty extends Response
	{
		private Response response;
		private Empty()
		{
			super(-1, "", "", -1L);
		}

	}
}
