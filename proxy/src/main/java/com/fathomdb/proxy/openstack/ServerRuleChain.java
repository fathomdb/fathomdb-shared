package com.fathomdb.proxy.openstack;

import java.util.Collections;
import java.util.List;

import com.fathomdb.proxy.htaccess.Directive;
import com.fathomdb.proxy.htaccess.DirectoryIndexDirective;
import com.fathomdb.proxy.htaccess.ScopeDirective;

public class ServerRuleChain {

	private final List<ScopeDirective> rules;

	public ServerRuleChain(List<ScopeDirective> rules) {
		this.rules = rules;
	}

	public Iterable<String> getDocumentIndexes() {
		DirectoryIndexDirective lowest = null;
		// TODO: Hack. We need to walk the whole tree, for one thing, not just 2
		// levels
		for (ScopeDirective rule : rules) {
			for (Directive child : rule.getChildren()) {
				if (child instanceof DirectoryIndexDirective) {
					lowest = (DirectoryIndexDirective) child;
				}
			}
		}

		if (lowest == null) {
			return Collections.emptyList();
		}

		return lowest.getDirectoryIndexUrls();
	}
}
