package com.fathomdb.proxy.htaccess;

public class HtaccessTokenizer {
	final String s;
	final int length;

	int pos = 0;

	public HtaccessTokenizer(String s) {
		this.s = s;
		this.length = s.length();
	}

	public String pop() {
		String token = poll();
		if (token == null) {
			throw new IllegalArgumentException("Unexpected end of tokens");
		}

		return token;
	}

	public String poll() {
		char firstChar = 0;
		while (pos < length) {
			firstChar = s.charAt(pos);
			if (firstChar != ' ') {
				break;
			}
			pos++;
		}

		if (pos >= length) {
			return null;
		}

		int start;
		int end;
		if (firstChar == '\"' || firstChar == '\'') {
			start = pos + 1;
			end = -1;

			for (int i = start; i < length; i++) {
				if (s.charAt(i) == firstChar) {
					end = i;
					break;
				}
			}

			if (end == -1) {
				throw new IllegalArgumentException("Unclosed quotes in line");
			}
		} else {
			start = pos;
			end = length;
			for (int i = start; i < length; i++) {
				if (s.charAt(i) == ' ') {
					end = i;
					break;
				}
			}
		}

		pos = end + 1;
		return s.substring(start, end);
	}
}
