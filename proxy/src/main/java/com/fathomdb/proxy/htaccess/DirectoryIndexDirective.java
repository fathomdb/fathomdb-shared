package com.fathomdb.proxy.htaccess;

import java.util.Arrays;
import java.util.List;

import com.fathomdb.proxy.http.rules.ServerRuleVisitor;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class DirectoryIndexDirective extends Directive {
	private final String[] urls;

	public DirectoryIndexDirective(ParseDirectiveNode node, String[] urls) {
		super(node);
		this.urls = urls;
	}

	public static DirectoryIndexDirective parse(ParseDirectiveNode node) {
		String s = node.arguments;

		HtaccessTokenizer tokens = new HtaccessTokenizer(s);

		List<String> urls = Lists.newArrayList();
		try {
			while (true) {
				String token = tokens.poll();
				if (token == null) {
					break;
				}
				urls.add(token);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Error parsing directive", e);
		}

		if (urls.isEmpty()) {
			throw new IllegalArgumentException("Expected at least one default url");
		}

		String[] urlsArray = urls.toArray(new String[urls.size()]);
		return new DirectoryIndexDirective(node, urlsArray);
	}

	@Override
	public String toString() {
		return "DirectoryIndexDirective [urls=" + Joiner.on(',').join(urls) + super.toStringHelper() + "]";
	}

	public Iterable<String> getDirectoryIndexUrls() {
		// TODO: Where the array => iterable function ??
		return Arrays.asList(urls);
	}

	@Override
	public void accept(ServerRuleVisitor visitor) {
		visitor.visit(this);
	}

}
