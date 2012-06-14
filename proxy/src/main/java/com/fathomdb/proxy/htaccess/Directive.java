package com.fathomdb.proxy.htaccess;

import com.fathomdb.proxy.http.rules.ServerRuleVisitor;

public abstract class Directive {
	public final ParseNode node;

	protected Directive(ParseNode node) {
		this.node = node;
	}

	protected String toStringHelper() {
		return "";
		// return ", node=" + node;
	}

	public abstract void accept(ServerRuleVisitor visitor);
}
