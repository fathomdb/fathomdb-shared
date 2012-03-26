package com.fathomdb.proxy.htaccess;

import java.util.List;

import com.google.common.collect.Lists;

public class ScopeDirective extends Directive {

	final List<Directive> children = Lists.newArrayList();

	protected ScopeDirective(ParseNode node) {
		super(node);
	}

	void addChild(Directive child) {
		children.add(child);
	}

	protected String toStringHelper() {
		return ", children=" + children + super.toStringHelper();
	}
	
}
