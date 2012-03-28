package com.fathomdb.proxy.http.server;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.fathomdb.proxy.cache.CacheFile;
import com.fathomdb.proxy.http.client.HttpClientPool;
import com.fathomdb.proxy.http.client.HttpClient;
import com.fathomdb.proxy.http.config.FilesystemHostConfigProvider;
import com.fathomdb.proxy.http.config.HostConfigProvider;
import com.fathomdb.proxy.openstack.OpenstackClientPool;
import com.fathomdb.proxy.openstack.OpenstackCredentials;

public class HttpProxyServer {
	static final Logger log = Logger.getLogger(HttpProxyServer.class);

	private final int port;

	public HttpProxyServer(int port) {
		this.port = port;
	}

	public void run() throws InterruptedException {
		// Configure the server.
		ServerBootstrap bootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));

		HttpClient client = new HttpClient();
		client.start();

		HttpClientPool httpClientPool = new HttpClientPool(client);
		OpenstackClientPool openstackClientPool = new OpenstackClientPool(
				httpClientPool);

		CacheFile cache = CacheFile.open(new File("cachedata000"));
		log.info("Opened cache file: " + cache);

		HostConfigProvider configProvider = new FilesystemHostConfigProvider(new File("hosts"));
		RequestHandlerProvider requestHandlerProvider = new RequestHandlerProvider(configProvider,
				cache, httpClientPool, openstackClientPool);
		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(new HttpProxyServerPipelineFactory(
				requestHandlerProvider));

		// Bind and start to accept incoming connections.
		bootstrap.bind(new InetSocketAddress(port));

		while (true) {
			Thread.sleep(10000);
			cache.writeMetadata();
		}
	}

	public static void main(String[] args) throws InterruptedException {
		int port;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		} else {
			port = 8888;
		}
		new HttpProxyServer(port).run();
	}
}
