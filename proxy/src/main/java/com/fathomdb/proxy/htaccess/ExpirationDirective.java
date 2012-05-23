package com.fathomdb.proxy.htaccess;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.fathomdb.proxy.utils.EnumUtils;
import com.google.common.base.Splitter;

public class ExpirationDirective extends Directive {
	protected final ExpirationTimeout timeout;

	public enum ExpirationBase {
		Access, Now, Modification
	};

	enum TimeUnit {
		Year(365 * 24 * 60 * 60), Month(30 * 24 * 60 * 60), Week(7 * 24 * 60 * 60), Day(24 * 60 * 60), Hour(60 * 60), Minute(
				60), Second(1);

		final int seconds;

		private TimeUnit(int seconds) {
			this.seconds = seconds;
		}

		public int getSeconds() {
			return seconds;
		}
	};

	public static class ExpirationTimeout {
		final ExpirationBase base;
		final int addSeconds;

		private ExpirationTimeout(ExpirationBase base, int addSeconds) {
			this.base = base;
			this.addSeconds = addSeconds;
		}

		@Override
		public String toString() {
			return "ExpirationTimeout [ " + base + " + " + addSeconds + "s ]";
		}

		public static ExpirationTimeout parse(String s) {
			Iterator<String> tokens = Splitter.on(' ').split(s).iterator();

			// ExpiresDefault "<base> [plus] {<num> <type>}*"
			// ExpiresByType type/encoding "<base> [plus] {<num> <type>}*"
			// where <base> is one of:
			//
			// access
			// now (equivalent to 'access')
			// modification
			// The plus keyword is optional. <num> should be an integer value
			// [acceptable to atoi()], and <type> is one of:
			//
			// years
			// months
			// weeks
			// days
			// hours
			// minutes
			// seconds

			try {
				String baseToken = tokens.next();
				ExpirationBase base = EnumUtils.valueOfCaseInsensitive(ExpirationBase.class, baseToken);
				int addSeconds = 0;

				while (tokens.hasNext()) {
					String token = tokens.next();
					if ("plus".equals(token)) {
						continue;
					}

					int count = Integer.parseInt(token);
					String timeUnitToken = tokens.next();
					if (timeUnitToken.endsWith("s")) {
						timeUnitToken = timeUnitToken.substring(0, timeUnitToken.length() - 1);
					}
					TimeUnit timeUnit = EnumUtils.valueOfCaseInsensitive(TimeUnit.class, timeUnitToken);

					addSeconds += timeUnit.getSeconds() * count;
				}

				return new ExpirationTimeout(base, addSeconds);
			} catch (NoSuchElementException e) {
				throw new IllegalArgumentException("Error parsing timeout");
			}
		}

		public ExpirationBase getBase() {
			return base;
		}

		public int getAddSeconds() {
			return addSeconds;
		}
	}

	ExpirationDirective(ParseDirectiveNode node, ExpirationTimeout timeout) {
		super(node);
		this.timeout = timeout;
	}

	public ExpirationTimeout getTimeout() {
		return timeout;
	}

	@Override
	protected String toStringHelper() {
		return ", timeout=" + timeout + super.toStringHelper();
	}

}
