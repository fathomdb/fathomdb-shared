package com.fathomdb.proxy.htaccess;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;

import com.google.common.base.Objects;

public class HtaccessParser implements Closeable {
	private final BufferedReader reader;
	private final ParseScopeNode root;

	public ParseScopeNode getRoot() {
		return root;
	}

	public HtaccessParser(BufferedReader reader) {
		this.reader = reader;
		this.root = new ParseScopeNode(null, null);
		nodeStack.push(root);
	}

	public HtaccessParser(InputStream is) {
		this(new BufferedReader(new InputStreamReader(is)));
	}

	final Stack<ParseScopeNode> nodeStack = new Stack<ParseScopeNode>();

	/**
	 * Read a line, combining any continuation characters
	 * 
	 * @return
	 * @throws IOException
	 */
	String readConfigLine() throws IOException {
		StringBuilder sb = null;

		while (true) {
			String line = reader.readLine();
			if (line == null) {
				if (sb != null)
					return sb.toString();
				return null;
			}
			if (line.endsWith("\\")) {
				// Line continuation
				if (sb == null) {
					sb = new StringBuilder(line);
				} else {
					sb.append(line);
				}
			} else {
				if (sb == null) {
					return line;
				} else {
					sb.append(line);
					return sb.toString();
				}
			}
		}
	}

	void parse() throws IOException {
		while (true) {
			String line = readConfigLine();
			if (line == null)
				break;
			line = line.trim();
			if (line.isEmpty()) {
				// Ignore blank lines
				continue;
			}

			char lineFirstChar = line.charAt(0);
			if (lineFirstChar == '#') {
				// Ignore comments
				continue;
			}

			String commandName = parseCommandName(line);
			if (commandName.isEmpty())
				continue;

			char commandFirstChar = commandName.charAt(0);
			if (commandFirstChar == '<') {
				String key = commandName;

				// The key won't always end with > (though the line should)
				// e.g. <Files *.nocache.*> => "<Files"
				if (key.endsWith(">")) {
					key = key.substring(0, key.length() - 1);
				}

				if (key.startsWith("</")) {
					// </key>
					key = key.substring(2);

					ParseScopeNode node = nodeStack.peek();
					if (!Objects.equal(node.key, key)) {
						throw new IllegalArgumentException("Expected </"
								+ node.key + ">, found </" + key + ">");
					}
					nodeStack.pop();

					if (nodeStack.isEmpty()) {
						throw new IllegalStateException();
					}
				} else {
					// <key>
					key = key.substring(1);

					String arguments = line.substring(commandName.length());
					arguments = arguments.trim();

					if (arguments.endsWith(">")) {
						arguments = arguments.substring(0,
								arguments.length() - 1);
					}

					arguments = removeQuotes(arguments);

					ParseScopeNode node = new ParseScopeNode(key, arguments);

					ParseScopeNode parent = nodeStack.peek();
					parent.add(node);

					nodeStack.push(node);
				}
			} else {
				String arguments = line.substring(commandName.length());
				arguments = arguments.trim();

				arguments = removeQuotes(arguments);

				ParseDirectiveNode node = new ParseDirectiveNode(commandName,
						arguments);
				ParseScopeNode parent = nodeStack.peek();
				parent.add(node);
			}
		}
	}

	private static String removeQuotes(String s) {
		if (s == null)
			return null;

		int len = s.length();
		if (len < 2)
			return s;

		char firstChar = s.charAt(0);
		char lastChar = s.charAt(len - 1);
		if (firstChar == '\"' && lastChar == '\"') {
			return s.substring(1, len - 1);
		} else {
			return s;
		}
	}

	private String parseCommandName(String line) {
		if (line.length() == 0)
			return "";

		char firstChar = line.charAt(0);

		int start;
		int end;
		if (firstChar == '\"' || firstChar == '\'') {
			start = 1;
			end = -1;

			for (int i = start; i < line.length(); i++) {
				if (line.charAt(i) == firstChar) {
					end = i;
					break;
				}
			}

			if (end == -1)
				throw new IllegalArgumentException("Unclosed quotes in line");
		} else {
			start = 0;
			end = line.length();

			for (int i = start; i < line.length(); i++) {
				if (line.charAt(i) == ' ') {
					end = i;
					break;
				}
			}
		}
		return line.substring(start, end);
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}
}
