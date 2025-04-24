package com.stresster.resources;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

import org.json.JSONObject;

import com.google.gson.annotations.SerializedName;

@Data
@Builder(builderClassName = "Builder")
public class Request
{
	private String uri;
	@SerializedName("http_method") private String method;
	private String cookie;
	@SerializedName("request_headers") private Map<String, Object> headers;
	@SerializedName("request_data") private String requestBody;
	@SerializedName("params") private Map<String, Object> queryParams;
	@SerializedName("form_data_param_name") private String formDataParamName;
}
