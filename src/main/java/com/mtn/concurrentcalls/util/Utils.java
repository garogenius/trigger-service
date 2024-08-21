package com.mtn.concurrentcalls.util;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {
	public static String getLocalZonedDateTime() {
		var dateTime = ZonedDateTime.now();
		var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		return dateTime.format(formatter);
	}

}
