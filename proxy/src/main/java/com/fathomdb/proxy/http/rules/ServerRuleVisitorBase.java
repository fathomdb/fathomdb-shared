package com.fathomdb.proxy.http.rules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.netty.handler.codec.http.HttpResponse;

import com.fathomdb.proxy.htaccess.Directive;
import com.fathomdb.proxy.htaccess.FilesDirective;
import com.fathomdb.proxy.http.vfs.VfsItemResolver.Resolved;

public class ServerRuleVisitorBase extends ServerRuleVisitor {
	final HttpResponse response;
	final Resolved resolved;

	public ServerRuleVisitorBase(Resolved resolved, HttpResponse response) {
		this.resolved = resolved;
		this.response = response;
	}

	@Override
	public void visit(ServerRuleChain serverRuleChain) {
	}

	@Override
	public void visitGeneric(Directive directive) {
	}

	@Override
	public boolean visit(FilesDirective filesDirective) {
		String filename = resolved.pathItem.getName();

		Pattern regex = filesDirective.getRegex();
		Matcher matcher = regex.matcher(filename);
		if (matcher.matches()) {
			visitGeneric(filesDirective);
			return true;
		} else {
			return false;
		}
	}
}
