package com.fathomdb.proxy.htaccess.expires;

import com.fathomdb.proxy.htaccess.Directive;
import com.fathomdb.proxy.htaccess.ParseDirectiveNode;
import com.fathomdb.proxy.htaccess.ParseNode;
import com.fathomdb.proxy.htaccess.Parsers;
import com.fathomdb.proxy.http.rules.ServerRuleVisitor;

public class ExpiresActive extends Directive {

	final boolean active;

	protected ExpiresActive(ParseNode node, boolean active) {
		super(node);
		this.active = active;
	}

	public static ExpiresActive parse(ParseDirectiveNode node) {
		boolean active = Parsers.parseOnOff(node.arguments);
		return new ExpiresActive(node, active);
	}

	@Override
	public String toString() {
		return "ExpiresActive [active=" + active + super.toStringHelper() + "]";
	}

	@Override
	public void accept(ServerRuleVisitor visitor) {
		visitor.visit(this);
	}

	public boolean isActive() {
		return active;
	}

}
