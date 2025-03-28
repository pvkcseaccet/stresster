package com.stresster.resources;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.annotations.SerializedName;
import com.stresster.domain.TestType;

@Builder(builderClassName = "Builder")
@Getter
public class TestProps
{
	@SerializedName("test_data_file_path") private String testDataFilePath;
	private int iterations;
	@SerializedName("test_type") private String testType;
	private String domain;
	@SerializedName("urls_to_test") private String urlsToTest;

	public Set<String> getURLsToTest()
	{
		return Arrays.stream(urlsToTest.split(","))
			.collect(Collectors.toSet());
	}

	public TestType getTestType()
	{
		return TestType.valueOf(this.testType);
	}
}
