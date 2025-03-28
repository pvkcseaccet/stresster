package com.stresster.parser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import com.stresster.resources.Request;
import com.stresster.resources.TestProps;

public class DataFetcher
{
	public static Map<String, List<Request>> get(String requestFile) throws IOException
	{
		String requestContent = FileUtils.readFileToString(new File(requestFile), StandardCharsets.UTF_8);
		return DataParser.getParsedRequestList(requestContent)
			.stream()
			.collect(Collectors.groupingBy(Request::getUri, Collectors.toList()));
	}

	public static TestProps getProps(String propsFile) throws IOException
	{
		String requestContent = FileUtils.readFileToString(new File(propsFile), StandardCharsets.UTF_8);
		return DataParser.getParsedTestProps(requestContent);
	}
}
