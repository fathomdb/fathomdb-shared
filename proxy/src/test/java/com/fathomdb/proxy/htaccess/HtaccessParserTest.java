package com.fathomdb.proxy.htaccess;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class HtaccessParserTest {

	@Test
	public void test1() throws IOException {
		ParseScopeNode root = parse("htaccess1");
		dump(root);
	}

	@Test
	public void test2() throws IOException {
		ParseScopeNode root = parse("htaccess2");
		dump(root);
	}

	private void dump(ParseScopeNode root) {
		Directive directive = root.compile();
		System.out.println(directive);

		// for (ParseNode child : root.getChildren()) {
		// Directive directive = child.compile();
		// System.out.println(directive);
		// }
		// System.out.println(root);
	}

	private ParseScopeNode parse(String s) throws IOException {
		InputStream is = getClass().getResourceAsStream("/" + s);

		HtaccessParser parser = new HtaccessParser(is);

		parser.parse();

		ParseScopeNode root = parser.getRoot();
		return root;
	}

}
