package com.fathomdb.ratelimit;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import javax.inject.Inject;

import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.ConnectionFactoryBuilder.Protocol;
import net.spy.memcached.FailureMode;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.auth.PlainCallbackHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.Configuration;
import com.google.common.base.Joiner;

public class RateLimitSystem implements Closeable {
	private static final Logger log = LoggerFactory.getLogger(RateLimitSystem.class);

	final List<InetSocketAddress> memcacheAddresses;

	private MemcachedClient client;

	final ConnectionFactoryBuilder builder;

	@Inject
	public RateLimitSystem(Configuration config) {
		this.memcacheAddresses = config.lookupList("ratelimit.memcache.servers", new InetSocketAddress[0]);

		ConnectionFactoryBuilder builder = new ConnectionFactoryBuilder();
		builder.setProtocol(Protocol.BINARY);
		builder.setOpTimeout(2000);
		builder.setFailureMode(FailureMode.Cancel);

		String username = config.find("ratelimit.memcache.username");
		if (username != null) {
			String password = config.get("ratelimit.memcache.password");
			AuthDescriptor auth = new AuthDescriptor(new String[] { "CRAM-MD5" }, new PlainCallbackHandler(username,
					password));

			builder.setAuthDescriptor(auth);
		}

		this.builder = builder;

		if (!memcacheAddresses.isEmpty()) {
			log.info("Using memcache: " + Joiner.on(",").join(memcacheAddresses));

			try {
				this.client = new MemcachedClient(builder.build(), memcacheAddresses);
			} catch (IOException e) {
				throw new IllegalArgumentException("Error building memcache client", e);
			}
		} else {
			log.info("Memcache not configured; will rate-limit using local counts");
			this.client = null;
		}
	}

	@Override
	public void close() {
		if (client != null) {
			client.shutdown();
			client = null;
		}
	}

	MemcachedClient getClient() {
		return client;
	}
}
