package com.fathomdb.proxy.http.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.cache.Cache;
import com.fathomdb.config.ConfigurationManager;
import com.fathomdb.config.UserSignalHandler;
import com.fathomdb.proxy.http.client.HttpClient;
import com.fathomdb.proxy.http.config.HttpProxyHostConfigProvider;
import com.fathomdb.proxy.http.inject.ProxyServerModule;
import com.fathomdb.proxy.openstack.fs.OpenstackDirectoryCache;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class HttpProxyServer {
	static final Logger log = LoggerFactory.getLogger(HttpProxyServer.class);

	private final int port;

	public HttpProxyServer(int port) {
		this.port = port;
	}

	public void run() throws InterruptedException, IOException {
		// Configure the server.
		ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

		Injector injector = Guice.createInjector(new ProxyServerModule());

		HttpClient client = injector.getInstance(HttpClient.class);
		client.start();

		ConfigurationManager configuration = ConfigurationManager.INSTANCE;

		OpenstackDirectoryCache openstackContainerMetadataCache = injector.getInstance(OpenstackDirectoryCache.class);
		openstackContainerMetadataCache.initialize();

		configuration.register(openstackContainerMetadataCache);

		HttpProxyHostConfigProvider configProvider = injector.getInstance(HttpProxyHostConfigProvider.class);
		configProvider.initialize();
		configuration.register(configProvider);

		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(injector.getInstance(HttpProxyServerPipelineFactory.class));

		// Bind and start to accept incoming connections.
		log.info("Listening on port {}", port);
		bootstrap.bind(new InetSocketAddress(port));

		UserSignalHandler.install();

		Cache cache = injector.getInstance(Cache.class);

		while (true) {
			Thread.sleep(10000);
			cache.writeMetadata();
		}
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		int port;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		} else {
			port = 8888;
		}
		new HttpProxyServer(port).run();
	}
}
