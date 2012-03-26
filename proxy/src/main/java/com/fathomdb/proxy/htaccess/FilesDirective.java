package com.fathomdb.proxy.htaccess;

import java.util.regex.Pattern;

public class FilesDirective extends ScopeDirective {
	private final Pattern regex;

	public FilesDirective(ParseScopeNode node, Pattern regex) {
		super(node);
		this.regex = regex;
	}

	// The <Files> directive limits the scope of the enclosed directives by
	// filename. It is comparable to the <Directory> and <Location> directives.
	// It should be matched with a </Files> directive. The directives given
	// within this section will be applied to any object with a basename (last
	// component of filename) matching the specified filename. <Files> sections
	// are processed in the order they appear in the configuration file, after
	// the <Directory> sections and .htaccess files are read, but before
	// <Location> sections. Note that <Files> can be nested inside <Directory>
	// sections to restrict the portion of the filesystem they apply to.
	//
	// The filename argument should include a filename, or a wild-card string,
	// where ? matches any single character, and * matches any sequences of
	// characters. Regular expressions can also be used, with the addition of
	// the ~ character. For example:
	//
	// <Files ~ "\.(gif|jpe?g|png)$">
	// would match most common Internet graphics formats. <FilesMatch> is
	// preferred, however.
	//
	// Note that unlike <Directory> and <Location> sections, <Files> sections
	// can be used inside .htaccess files. This allows users to control access
	// to their own files, at a file-by-file level.
	public static FilesDirective parse(ParseScopeNode node) {
		Pattern regex = wildcardToRegex(node.arguments);
		return new FilesDirective(node, regex);
	}

	public static Pattern wildcardToRegex(String match) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < match.length(); i++) {
			char c = match.charAt(i);

			switch (c) {
			case '*':
				sb.append(".*");
				break;

			case '?':
				sb.append(".");
				break;

			case '(':
			case ')':
			case '[':
			case ']':
			case '{':
			case '}':
			case '.':
			case '^':
			case '$':
			case '|':
			case '\\':
				sb.append('\\');
				sb.append(c);
				break;

			default:
				sb.append(c);
				break;
			}
		}

		// sb = "^" + sb + "$";

		return Pattern.compile(sb.toString());
	}

	@Override
	public String toString() {
		return "FilesDirective [regex=" + regex + super.toStringHelper() + "]";
	}

}
