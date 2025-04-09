package com.stresster.resources.reports;

import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.stresster.reports.HtmlReportGenerator;
import com.stresster.resources.Response;

@Getter
public class Iteration
{
	private int iterationNumber;
	private long apiCalls;
	private String executionTime;
	private int executionTimeinMillis;
	private List<ApiResponseDetail> apiResponseDetail;
	private int apiCallsSucceeded;
	private int apiCallsFailed;

	public Iteration(int iterationNumber, long apiCalls, String executionTime, List<ApiResponseDetail> apiResponseDetail)
	{
		this.iterationNumber = iterationNumber;
		this.apiCalls = apiCalls;
		this.executionTime = executionTime;
		this.executionTimeinMillis = StringUtils.isEmpty(this.executionTime) ? 0 :Integer.parseInt(this.executionTime.substring(0, this.executionTime.length()-1));
		this.apiResponseDetail = apiResponseDetail;
		this.apiCallsSucceeded = (int) this.getApiResponseDetail()
			.stream()
			.map(ApiResponseDetail::getResponses)
			.flatMap(List::stream)
			.map(Response::getStatusCode)
			.filter(HtmlReportGenerator.SUCCEEDED_REQUESTS)
			.count();
		this.apiCallsFailed = (int) (this.apiCalls - this.apiCallsSucceeded);
	}

}
