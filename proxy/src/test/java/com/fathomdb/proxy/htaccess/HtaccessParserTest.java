package com.fathomdb.proxy.htaccess;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class HtaccessParserTest {

	@Test
	public void test() throws IOException {
		InputStream is = getClass().getResourceAsStream("htaccess1");
		
		HtaccessParser parser = new HtaccessParser(is);
		
		parser.parse();
		
		ParseScopeNode root = parser.getRoot();
		
		for (ParseNode child : root.getChildren()) {
			Directive directive = child.compile();
			System.out.println(directive);
		}
		//System.out.println(root);
	}

}
