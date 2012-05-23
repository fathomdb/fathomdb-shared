package com.fathomdb.proxy.http.client;

import java.net.InetSocketAddress;
import java.net.URI;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.proxy.http.HttpScheme;

public class HttpClientConnection implements AutoCloseable {
	static final Logger log = LoggerFactory.getLogger(HttpClientConnection.class);

	private final HttpScheme scheme;
	private final String host;
	private final HttpClient client;

	private final int port;
	private ChannelFuture connectFuture;

	public HttpClientConnection(HttpClient client, HttpScheme scheme, String host, int port) {
		this.client = client;
		this.scheme = scheme;
		this.host = host;
		this.port = port;
	}

	public ChannelFuture connect() {
		boolean ssl = scheme == HttpScheme.HTTPS;

		this.connectFuture = client.getClientBootstrap(ssl).connect(new InetSocketAddress(host, port));

		return connectFuture;
	}

	public void doRequest(HttpRequest request, HttpResponseHandler listener) {
		Channel channel = getChannel();

		// Set some example cookies.
		// CookieEncoder httpCookieEncoder = new CookieEncoder(false);
		// httpCookieEncoder.addCookie("my-cookie", "foo");
		// httpCookieEncoder.addCookie("another-cookie", "bar");
		// request.setHeader(HttpHeaders.Names.COOKIE,
		// httpCookieEncoder.encode());

		HttpProxyClientHandler httpProxyClientHandler = channel.getPipeline().get(HttpProxyClientHandler.class);
		httpProxyClientHandler.setTarget(listener);

		log.info("Client sending request: " + request);

		// Send the HTTP request.
		channel.write(request);
	}

	public boolean isConnected() {
		if (connectFuture == null) {
			return false;
		}
		Channel channel = connectFuture.getChannel();
		return channel.isConnected();
	}

	public Channel getChannel() {
		Channel channel = connectFuture.getChannel();
		return channel;
	}

	public static String getHostHeader(URI url) {
		HttpScheme scheme = HttpScheme.parse(url.getScheme());
		int port = url.getPort();
		if (port == -1) {
			port = scheme.getPort();
		}
		String hostAndPort = url.getHost() + ":" + port;
		return hostAndPort;
	}

	@Override
	public void close() {
		// // Wait for the server to close the connection.
		// channel.getCloseFuture().awaitUninterruptibly();
		//
		// // Shut down executor threads to exit.
		// client.releaseExternalResources();

		if (connectFuture != null) {
			Channel channel = connectFuture.getChannel();
			channel.close();
		}
	}

}
