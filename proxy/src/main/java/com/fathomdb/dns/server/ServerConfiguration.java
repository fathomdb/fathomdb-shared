package com.fathomdb.dns.server;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

public class ServerConfiguration {

	final List<SocketAddress> bindingHosts = Lists.newArrayList();

	public ServerConfiguration() {
	}

	public int getThreadPoolSize() {
		return 4;
	}

	public Collection<SocketAddress> getBindingHosts() {
		return bindingHosts;
	}

	public void addBindingHost(SocketAddress socketAddress) {
		bindingHosts.add(socketAddress);
	}

}
