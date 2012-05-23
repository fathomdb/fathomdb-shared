package com.fathomdb.proxy.backend.relay;

public class BackendConnectionMap {
	final String prefix;
	final BackendConnectionPool pool;

	public BackendConnectionMap(String prefix, BackendConnectionPool pool) {
		this.prefix = prefix;
		this.pool = pool;
	}

	public String getPrefix() {
		return prefix;
	}

	public BackendConnectionPool getPool() {
		return pool;
	}

	public BackendConnection getClient() {
		return new BackendConnection(this);
	}

}
