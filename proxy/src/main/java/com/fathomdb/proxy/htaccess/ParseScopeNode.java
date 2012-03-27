package com.fathomdb.proxy.htaccess;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class ParseScopeNode extends ParseNode {

	public final String key;
	public final String arguments;

	final List<ParseNode> children = Lists.newArrayList();

	public List<ParseNode> getChildren() {
		return children;
	}

	public ParseScopeNode(String key, String arguments) {
		this.key = key;
		this.arguments = arguments;
	}

	public void add(ParseNode node) {
		children.add(node);
	}

	@Override
	public String toString() {
		return "ParseScopeNode [key=" + key + ", arguments=" + arguments
				+ ", children=" + children + "]";
	}

	public ScopeDirective compile() {
		ScopeDirective directive;
		
		if (key == null) {
			directive = new RootDirective(this);
		} else if (Objects.equal(key, "Files")) {
			directive = FilesDirective.parse(this);
		} else {
			throw new IllegalArgumentException("Unknown directive: " + key);
		}
		
		for (ParseNode child : children) {
			Directive compiledChild = child.compile();
			directive.addChild(compiledChild);
		}
		return directive;
	}
}
