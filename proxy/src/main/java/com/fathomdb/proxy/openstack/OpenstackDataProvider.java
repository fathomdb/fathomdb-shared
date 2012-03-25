package com.fathomdb.proxy.openstack;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.HttpMethod;
import com.fathomdb.proxy.http.server.GenericRequest;
import com.fathomdb.proxy.objectdata.ObjectDataProvider;
import com.fathomdb.proxy.objectdata.ObjectDataSink;

public class OpenstackDataProvider implements ObjectDataProvider {
	static final Logger log = Logger.getLogger(OpenstackDataProvider.class);

	final OpenstackClientPool openstackClientPool;

	final OpenstackCredentials openstackCredentials;

	public OpenstackDataProvider(OpenstackCredentials openstackCredentials,
			OpenstackClientPool openstackClientPool) {
		this.openstackCredentials = openstackCredentials;
		this.openstackClientPool = openstackClientPool;
	}


	@Override
	public ChannelFuture handle(GenericRequest request, final ObjectDataSink sink) {
		final String requestURI = request.getRequestURI();
		String hostAndPort = request.getHeader("Host");
		HttpMethod method = request.getMethod();

		final String objectPath = "bucketshop" + requestURI;

		log.debug("HandleRequest " + method + " " + hostAndPort + " "
				+ requestURI);

		// httpClientPool.getClient();

		int port = 0;

		int colonIndex = hostAndPort.indexOf(':');
		if (colonIndex != -1) {
		} else {
		}

		OpenstackStorageRequest upstreamRequest = null;

		if (method.equals(HttpMethod.GET)) {
			// Prepare the HTTP request.
			upstreamRequest = new OpenstackStorageRequest(requestURI);
		}

		if (upstreamRequest == null) {
			// TODO: Send a nice error
			throw new UnsupportedOperationException();
		}

		final OpenstackClient connection = openstackClientPool
				.getClient(openstackCredentials);

		Channel channel = request.getChannel();

		boolean cancellable = false;
		return Futures.on(channel, cancellable).poll(new Callable<ChannelFuture>() {
			@Override
			public ChannelFuture call() throws Exception {
				return connection.readObject(objectPath, sink);
			}
		}).poke();
	}

}
