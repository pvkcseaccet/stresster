package com.stresster.resources.reports;

import lombok.Builder;
import lombok.Getter;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.stresster.resources.Response;

@Getter
@Builder
public class ReportsContext
{
	private String testedDomain;
	private List<String> testedAPIs;
	private String testMode;
	private File htmlReportFile;
	private Map<Integer, Long> reports;
	private Map<Integer, Map<String, List<Response>>> apiResponses;
}
