package com.fathomdb.proxy.htaccess;

import com.fathomdb.proxy.http.rules.ServerRuleVisitor;
import com.fathomdb.utils.EnumUtils;

public class HeaderDirective extends Directive {
	private final Condition condition;
	private final Action action;
	private final String header;
	private final String value;

	enum Action {
		Set, Append, Merge, Add, Unset, Echo
	};

	enum Condition {
		OnSuccess, Always
	};

	public HeaderDirective(ParseDirectiveNode node, Condition condition, Action action, String header, String value) {
		super(node);
		this.condition = condition;
		this.action = action;
		this.header = header;
		this.value = value;
	}

	public static HeaderDirective parse(ParseDirectiveNode node) {
		String s = node.arguments;

		HtaccessTokenizer tokens = new HtaccessTokenizer(s);

		try {
			String token = tokens.pop();
			Condition condition = EnumUtils.valueOfCaseInsensitiveOrNull(Condition.class, token);
			if (condition != null) {
				token = tokens.pop();
			}

			Action action = EnumUtils.valueOfCaseInsensitive(Action.class, token);

			String header = tokens.pop();

			String value = null;

			switch (action) {
			case Add:
			case Append:
			case Merge:
			case Set:
				value = tokens.pop();
				break;
			}

			if (tokens.poll() != null) {
				throw new IllegalArgumentException("Unexpected tokens at end of directive");
			}

			return new HeaderDirective(node, condition, action, header, value);
		} catch (Exception e) {
			throw new IllegalArgumentException("Error parsing directive", e);
		}
	}

	@Override
	public String toString() {
		return "HeaderDirective [condition=" + condition + ", action=" + action + ", header=" + header + ", value="
				+ value + "]";
	}

	@Override
	public void accept(ServerRuleVisitor visitor) {
		visitor.visit(this);
	}
}
