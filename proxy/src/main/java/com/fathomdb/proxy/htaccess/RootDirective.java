package com.fathomdb.proxy.htaccess;

public class RootDirective extends ScopeDirective {
	public RootDirective(ParseScopeNode node) {
		super(node);
	}

	@Override
	public String toString() {
		return "RootDirective [" + super.toStringHelper() + "]";
	}

}
