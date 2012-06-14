package com.fathomdb.proxy.http.rules;

import com.fathomdb.proxy.htaccess.Directive;
import com.fathomdb.proxy.htaccess.DirectoryIndexDirective;
import com.fathomdb.proxy.htaccess.FilesDirective;
import com.fathomdb.proxy.htaccess.HeaderDirective;
import com.fathomdb.proxy.htaccess.RootDirective;
import com.fathomdb.proxy.htaccess.expires.ExpiresActive;
import com.fathomdb.proxy.htaccess.expires.ExpiresByType;
import com.fathomdb.proxy.htaccess.expires.ExpiresDefault;

public abstract class ServerRuleVisitor {

	public abstract void visit(ServerRuleChain serverRuleChain);

	public abstract void visitGeneric(Directive directive);

	public void visit(DirectoryIndexDirective directoryIndexDirective) {
		visitGeneric(directoryIndexDirective);
	}

	public boolean visit(RootDirective rootDirective) {
		visitGeneric(rootDirective);
		return true;
	}

	public boolean visit(FilesDirective filesDirective) {
		visitGeneric(filesDirective);
		return true;
	}

	public void visit(ExpiresActive expiresActive) {
		visitGeneric(expiresActive);
	}

	public void visit(HeaderDirective headerDirective) {
		visitGeneric(headerDirective);
	}

	public void visit(ExpiresByType expiresByType) {
		visitGeneric(expiresByType);
	}

	public void visit(ExpiresDefault expiresDefault) {
		visitGeneric(expiresDefault);
	}
}
