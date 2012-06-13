package com.fathomdb.proxy.http.client;

import javax.inject.Singleton;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

@Singleton
public class HttpClient {
	ClientBootstrap httpClientBootstrap;
	ClientBootstrap httpsClientBootstrap;

	public void start() {
		NioClientSocketChannelFactory channelFactory = new NioClientSocketChannelFactory(ThreadPools.BOSS_POOL,
				ThreadPools.WORKER_POOL);

		httpClientBootstrap = new ClientBootstrap(channelFactory);
		httpClientBootstrap.setPipelineFactory(new HttpProxyClientPipelineFactory(false));

		httpsClientBootstrap = new ClientBootstrap(channelFactory);
		httpsClientBootstrap.setPipelineFactory(new HttpProxyClientPipelineFactory(true));
	}

	public ClientBootstrap getClientBootstrap(boolean ssl) {
		if (ssl) {
			return httpsClientBootstrap;
		} else {
			return httpClientBootstrap;
		}
	}

}