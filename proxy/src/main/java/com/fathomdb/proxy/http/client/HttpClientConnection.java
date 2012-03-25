package com.fathomdb.proxy.http.client;

import java.net.InetSocketAddress;
import java.net.URI;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.HttpRequest;
import com.fathomdb.proxy.http.HttpScheme;

public class HttpClientConnection {
	static final Logger log = Logger.getLogger(HttpClientConnection.class);

	private final HttpScheme scheme;
	private final String host;
	private final HttpClient client;

	private final int port;
	private ChannelFuture connectFuture;

	public HttpClientConnection(HttpClient client, HttpScheme scheme,
			String host, int port) {
		this.client = client;
		this.scheme = scheme;
		this.host = host;
		this.port = port;
	}

	public ChannelFuture connect() {
		// private final URI uri;
		//
		// String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
		// String host = uri.getHost() == null ? "localhost" : uri.getHost();
		// int port = uri.getPort();
		// if (port == -1) {
		// if (scheme.equalsIgnoreCase("http")) {
		// port = 80;
		// } else if (scheme.equalsIgnoreCase("https")) {
		// port = 443;
		// }
		// }

		// if (!scheme.equalsIgnoreCase("http")
		// && !scheme.equalsIgnoreCase("https")) {
		// System.err.println("Only HTTP(S) is supported.");
		// return;
		// }

		boolean ssl = scheme == HttpScheme.HTTPS;

		// Start the connection attempt.
		this.connectFuture = client.bootstrap.connect(new InetSocketAddress(
				host, port));

		return connectFuture;
	}

	public void doRequest(HttpRequest request, HttpResponseListener target) {
		Channel channel = getChannel();

		// Set some example cookies.
		// CookieEncoder httpCookieEncoder = new CookieEncoder(false);
		// httpCookieEncoder.addCookie("my-cookie", "foo");
		// httpCookieEncoder.addCookie("another-cookie", "bar");
		// request.setHeader(HttpHeaders.Names.COOKIE,
		// httpCookieEncoder.encode());

		HttpProxyClientHandler httpProxyClientHandler = channel.getPipeline()
				.get(HttpProxyClientHandler.class);
		httpProxyClientHandler.setTarget(target);

		log.debug("Client sending request: " + request);

		// Send the HTTP request.
		channel.write(request);
	}

	public void close() {
		throw new UnsupportedOperationException();
		//
		// // Wait for the server to close the connection.
		// channel.getCloseFuture().awaitUninterruptibly();
		//
		// // Shut down executor threads to exit.
		// client.releaseExternalResources();
	}

	public boolean isConnected() {
		if (connectFuture == null)
			return false;
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

}
