package com.fathomdb.proxy.htaccess;

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

}
