package com.fathomdb.proxy.http.rules;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fathomdb.proxy.htaccess.HtaccessParser;
import com.fathomdb.proxy.htaccess.ParseScopeNode;
import com.fathomdb.proxy.htaccess.ScopeDirective;
import com.fathomdb.proxy.http.vfs.VfsItem;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class ServerRuleResolver {

	private final VfsItem root;

	public ServerRuleResolver(VfsItem root) {
		this.root = root;
	}

	public ServerRuleChain resolveServerRules(String path) {
		VfsItem current = root;

		List<ScopeDirective> rules = Lists.newArrayList();

		ScopeDirective systemRules = loadSystemDefaults();
		rules.add(systemRules);

		for (String pathToken : Splitter.on('/').omitEmptyStrings().split(path)) {
			current = current.findChild(pathToken);
			if (current == null) {
				break;
			}

			VfsItem ruleFile = current.findChild(".htaccess");
			if (ruleFile != null) {
				// TODO: Cache compiled rules based on their hashes
				ScopeDirective loaded = loadRules(ruleFile);
				rules.add(loaded);
			}
		}

		// TODO: Cache compiled chains (which we can do with their hashes)
		// TODO: Use linked list structure
		return new ServerRuleChain(rules);
	}

	private ScopeDirective loadRules(VfsItem ruleFile) {
		throw new UnsupportedOperationException();

		// InputStream is = getClass().getResourceAsStream(s);
		//
		// HtaccessParser parser = new HtaccessParser(is);
		//
		// parser.parse();
		//
		// ParseScopeNode root = parser.getRoot();
		//
		// return root.compile();
	}

	public ScopeDirective loadSystemDefaults() {
		// TODO: Cache this!!!

		InputStream is = getClass().getResourceAsStream("/htaccess.default");
		if (is == null) {
			throw new IllegalStateException("Cannot find resource: htaccess.default");
		}
		HtaccessParser parser = new HtaccessParser(is);

		try {
			parser.parse();
		} catch (IOException e) {
			throw new IllegalArgumentException("Error parsing system htaccess file", e);
		}

		ParseScopeNode root = parser.getRoot();

		return root.compile();
	}
}
