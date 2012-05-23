package com.fathomdb.proxy.htaccess;

public class Parsers {

	public static boolean parseOnOff(String s) {
		s = s.toLowerCase();
		if (s.equals("on")) {
			return true;
		}
		if (s.equals("off")) {
			return false;
		}

		throw new IllegalArgumentException("Expected On/Off, got " + s);
	}

}
