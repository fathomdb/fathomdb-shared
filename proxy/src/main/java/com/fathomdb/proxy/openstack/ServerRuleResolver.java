package com.fathomdb.proxy.openstack;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fathomdb.proxy.htaccess.HtaccessParser;
import com.fathomdb.proxy.htaccess.ParseScopeNode;
import com.fathomdb.proxy.htaccess.ScopeDirective;
import com.fathomdb.proxy.openstack.fs.OpenstackItem;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class ServerRuleResolver {

	private final OpenstackItem root;

	public ServerRuleResolver(OpenstackItem root) {
		this.root = root;
	}

	public ServerRuleChain resolveServerRules(String path) {
		OpenstackItem current = root;

		List<ScopeDirective> rules = Lists.newArrayList();

		ScopeDirective systemRules = loadSystemDefaults();
		rules.add(systemRules);

		for (String pathToken : Splitter.on('/').omitEmptyStrings().split(path)) {
			current = current.getChild(pathToken);
			if (current == null) {
				break;
			}

			OpenstackItem ruleFile = current.getChild(".htaccess");
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

	private ScopeDirective loadRules(OpenstackItem ruleFile) {
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
