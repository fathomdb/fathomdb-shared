package com.fathomdb.proxy.htaccess;

import java.util.List;

import com.google.common.collect.Lists;

public class ScopeDirective extends Directive {

	final List<Directive> children = Lists.newArrayList();

	public Iterable<Directive> getChildren() {
		return children;
	}

	protected ScopeDirective(ParseNode node) {
		super(node);
	}

	void addChild(Directive child) {
		children.add(child);
	}

	@Override
	protected String toStringHelper() {
		return ", children=" + children + super.toStringHelper();
	}

}
