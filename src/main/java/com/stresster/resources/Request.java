package com.stresster.resources;

import lombok.Builder;
import lombok.Data;

import org.json.JSONObject;

@Data
@Builder(builderClassName = "Builder")
public class Request
{
	private String url;
	private String uri;
	private String method;
	private String cookie;
	private JSONObject headers;
	private String requestBody;
	private JSONObject queryParams;
}
