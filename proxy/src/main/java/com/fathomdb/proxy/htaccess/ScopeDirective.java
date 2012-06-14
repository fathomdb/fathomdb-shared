package com.fathomdb.proxy.htaccess;

import java.util.List;

import com.fathomdb.proxy.http.rules.ServerRuleVisitor;
import com.google.common.collect.Lists;

public abstract class ScopeDirective extends Directive {

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

	protected void visitChildren(ServerRuleVisitor visitor) {
		for (Directive child : children) {
			child.accept(visitor);
		}
	}

}
