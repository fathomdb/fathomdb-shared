package com.fathomdb.proxy.htaccess;

import com.fathomdb.proxy.http.rules.ServerRuleVisitor;

public class RootDirective extends ScopeDirective {
	public RootDirective(ParseScopeNode node) {
		super(node);
	}

	@Override
	public String toString() {
		return "RootDirective [" + super.toStringHelper() + "]";
	}

	@Override
	public void accept(ServerRuleVisitor visitor) {
		if (visitor.visit(this)) {
			visitChildren(visitor);
		}
	}

}
