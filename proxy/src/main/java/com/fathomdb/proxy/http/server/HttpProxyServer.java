package com.fathomdb.proxy.http.server;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.cache.CacheFile;
import com.fathomdb.config.ConfigurationManager;
import com.fathomdb.config.UserSignalHandler;
import com.fathomdb.proxy.http.client.HttpClient;
import com.fathomdb.proxy.http.client.HttpClientPool;
import com.fathomdb.proxy.http.config.HttpProxyHostConfigProvider;
import com.fathomdb.proxy.http.logger.RequestLogger;
import com.fathomdb.proxy.openstack.OpenstackClientPool;
import com.fathomdb.proxy.openstack.fs.OpenstackDirectoryCache;

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

		HttpClient client = new HttpClient();
		client.start();

		HttpClientPool httpClientPool = new HttpClientPool(client);
		OpenstackClientPool openstackClientPool = new OpenstackClientPool(httpClientPool);

		CacheFile cache = CacheFile.open(new File("cachedata000"));
		log.info("Opened cache file: " + cache);

		ConfigurationManager configuration = ConfigurationManager.INSTANCE;

		OpenstackDirectoryCache openstackContainerMetadataCache = new OpenstackDirectoryCache(openstackClientPool);
		openstackContainerMetadataCache.initialize();

		configuration.register(openstackContainerMetadataCache);

		File logDir = new File("logs");
		logDir.mkdir();
		File logFile = new File(logDir, "log" + System.currentTimeMillis() + ".log");
		RequestLogger logger = new RequestLogger(logFile);

		HttpProxyHostConfigProvider configProvider = new HttpProxyHostConfigProvider(new File("hosts"));
		configProvider.initialize();

		configuration.register(configProvider);

		RequestHandlerProvider requestHandlerProvider = new RequestHandlerProvider(logger, configProvider,
				openstackContainerMetadataCache, cache, httpClientPool, openstackClientPool);
		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(new HttpProxyServerPipelineFactory(requestHandlerProvider));

		// Bind and start to accept incoming connections.
		bootstrap.bind(new InetSocketAddress(port));

		UserSignalHandler.install();

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
