package com.stresster.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum StressterExceptionStore
{
	REQUEST_LIST_EMPTY(new StressterException("No URL matches URLs given in test-props file."), true),
	INVALID_ARGUMENT_RECEIVED_FROM_CLIENT(new StressterException("Invalid Test Arguments Passed."), true),
	TEST_EXECUTION_FAILED(new StressterException("Test Execution Failed."), true),
	REPORT_GENERATION_FAILED(new StressterException("Report Generation Failed."), true);

	StressterExceptionStore(StressterException ex, boolean needLog)
	{
		this.exception = ex;
		this.needLog = needLog;
	}

	private static final Logger LOGGER = Logger.getLogger(StressterExceptionStore.class.getName());
	//Please change it in case if calling hierarchy of the method changes, else devs will miss out the exception's originating class
	private static final int LOGGER_METHOD_CALL_HIERARCHY = 3;

	private StressterException exception;
	private boolean needLog;

	public StressterException _new()
	{
		return _new(null);
	}

	public StressterException _new(Throwable throwable)
	{
		if(this.needLog)
		{
			String logMessage = "Class: " + getCallerClassName() ;
			if (throwable == null)
			{
				logMessage += " Exception: " + this.exception.getMessage();
				LOGGER.log(Level.SEVERE, logMessage);
			}
			else
			{
				logMessage += " Exception: " + throwable.getMessage();
				LOGGER.log(Level.SEVERE, logMessage, throwable);
			}
		}
		return this.exception;
	}

	public static String getCallerClassName()
	{
		return StackWalker
			.getInstance()
			.walk(frames -> frames.skip(LOGGER_METHOD_CALL_HIERARCHY)
				.findFirst()
				.map(StackWalker.StackFrame::getClassName)
				.orElse("Caller Class Not Found."));
	}
}
