package com.fathomdb.proxy.htaccess.expires;

import com.fathomdb.proxy.htaccess.ParseDirectiveNode;
import com.fathomdb.proxy.http.rules.ServerRuleVisitor;

public class ExpiresDefault extends ExpirationDirective {
	ExpiresDefault(ParseDirectiveNode node, ExpirationTimeout timeout) {
		super(node, timeout);
	}

	public static ExpiresDefault parse(ParseDirectiveNode node) {
		ExpirationTimeout timeout = ExpirationTimeout.parse(node.arguments);
		return new ExpiresDefault(node, timeout);
	}

	@Override
	public String toString() {
		return "ExpiresDefault [" + super.toStringHelper() + "]";
	}

	@Override
	public void accept(ServerRuleVisitor visitor) {
		visitor.visit(this);
	}

}
