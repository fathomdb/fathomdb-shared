package com.fathomdb.proxy.http;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Dates {

	public static final String DATE_PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
	public static final String DATE_PATTERN_JSON = "yyyy-MM-dd'T'HH:mm:ss.SSS";

	public static String format(Date lastModified) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN_RFC1123);
		return dateFormat.format(lastModified);
	}

	public static Date parse(String value) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN_RFC1123);
		return dateFormat.parse(value);
	}

	public static Date parseJson(String value) throws ParseException {
		int dotIndex = value.indexOf('.');
		if (dotIndex != -1) {
			int endIndex = dotIndex + 4;
			if (endIndex > value.length()) {
				endIndex = value.length();
			}
			value = value.substring(0, endIndex);
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN_JSON);
		return dateFormat.parse(value);
	}

}
