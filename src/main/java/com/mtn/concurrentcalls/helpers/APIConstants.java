package com.mtn.concurrentcalls.helpers;

import lombok.Data;

@Data
public class APIConstants {

	private APIConstants() {
		throw new IllegalStateException("Constants class");
	}

	public static final String LOG_START_START = "SAS AML CONCURRENT";
	public static final String LOG_START = "START";
	public static final String LOG_END = "END";
	public static final String URL_SAS = "URL TO SAS : {}";
	public static final String LOG_STRING_FORMAT = "%s :: %s :: %s";
	public static final String ERROR_DURING_DATA_FETCH = "Failed to post data to system layer. Error from Core system";
	public static final String SUCCESS = "Success";
	public static final String FAILURE = "Failed";
	public static final String CONNECTED= "Connected";
	public static final String NOT_CONNECTED="Database connection not available";

}
