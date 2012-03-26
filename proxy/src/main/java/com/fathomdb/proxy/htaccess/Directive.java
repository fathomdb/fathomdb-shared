package com.fathomdb.proxy.htaccess;

public class Directive {
	public final ParseNode node;

	protected Directive(ParseNode node) {
		this.node = node;
	}

	protected String toStringHelper() {
		return "";
//		return ", node=" + node;
	}

}
