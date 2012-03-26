package com.fathomdb.proxy.htaccess;

import com.google.common.base.Objects;

public class ParseDirectiveNode extends ParseNode {

	public final String key;
	public final String arguments;

	public ParseDirectiveNode(String key, String arguments) {
		this.key = key;
		this.arguments = arguments;
	}

	@Override
	public String toString() {
		return "ParseDirectiveNode [commandName=" + key
				+ ", arguments=" + arguments + "]";
	}

	@Override
	public Directive compile() {
		if (Objects.equal(key, "ExpiresDefault")) {
			return ExpiresDefault.parse(this);
		} else if (Objects.equal(key, "ExpiresActive")) {
			return ExpiresActive.parse(this);
		} else if (Objects.equal(key, "Header")) {
			return HeaderDirective.parse(this);
		} else if (Objects.equal(key, "DirectoryIndex")) {
			return DirectoryIndexDirective.parse(this);
		} else {
			throw new IllegalArgumentException("Unknown directive: " + key);
		}
	}

}
