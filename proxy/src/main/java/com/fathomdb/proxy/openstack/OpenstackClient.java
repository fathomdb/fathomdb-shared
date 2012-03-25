package com.fathomdb.proxy.openstack;

import java.net.URI;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

import com.fathomdb.proxy.http.HttpScheme;
import com.fathomdb.proxy.http.client.HttpClientConnection;
import com.fathomdb.proxy.objectdata.ObjectDataSink;

/**
 * It would be nice to use the same codebase as the main OpenStack Java binding.
 * However, this is pretty specialized for Swift & Async
*/
public class OpenstackClient {
	final OpenstackClientPool openstackClientPool;
	private final OpenstackCredentials credentials;

	HttpClientConnection keystoneHttpClient;
	HttpClientConnection swiftHttpClient;

	public OpenstackClient(OpenstackClientPool openstackClientPool,
			OpenstackCredentials credentials) {
		this.openstackClientPool = openstackClientPool;
		this.credentials = credentials;
	}

	KeystoneResponseListener keystoneAuthentication;
	ObjectListener httpListener;

	public ChannelFuture authenticate() {
		URI authUrl = credentials.getAuthUrl();

		HttpScheme scheme = HttpScheme.parse(authUrl.getScheme());
		int port = authUrl.getPort();
		if (port == -1) {
			port = scheme.getPort();
		}
		String hostAndPort = authUrl.getHost() + ":" + port;

		if (keystoneHttpClient == null) {
			keystoneHttpClient = openstackClientPool.httpClientPool.getClient(
					scheme, hostAndPort);
		}

		if (!keystoneHttpClient.isConnected()) {
			return keystoneHttpClient.connect();
		}

		if (keystoneAuthentication == null
				|| !keystoneAuthentication.isAuthenticated()) {
			keystoneAuthentication = new KeystoneResponseListener(
					keystoneHttpClient.getChannel());

			HttpRequest authRequest = new DefaultHttpRequest(
					HttpVersion.HTTP_1_1, HttpMethod.POST, authUrl.getPath());
			authRequest.setHeader(HttpHeaders.Names.HOST, hostAndPort);

			// TODO: Use keep-alive
			authRequest.setHeader(HttpHeaders.Names.CONNECTION,
					HttpHeaders.Values.CLOSE);

			authRequest.setHeader(HttpHeaders.Names.CONTENT_TYPE,
					"application/xml");
			authRequest.setHeader(HttpHeaders.Names.ACCEPT, "application/xml");

			// We hard-code this for simplicity
			// TODO: Use XML writer so we get escaping
			String username = credentials.getUsername();
			String password = credentials.getPassword();
			String tenantName = credentials.getTenant();
			String postBody = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
					+ "<auth xmlns=\"http://docs.openstack.org/identity/api/v2.0\" tenantName=\""
					+ tenantName
					+ "\">"
					+ "<passwordCredentials username=\""
					+ username + "\" password=\"" + password + "\"/></auth>";

			ChannelBuffer contentBuffer = ChannelBuffers.copiedBuffer(postBody,
					CharsetUtil.UTF_8);
			authRequest.setContent(contentBuffer);

			authRequest.setHeader(HttpHeaders.Names.CONTENT_LENGTH,
					contentBuffer.readableBytes());
			keystoneHttpClient.doRequest(authRequest, keystoneAuthentication);

			return keystoneAuthentication.getFuture();
		}

		return null;
	}

	public ChannelFuture connectToSwift() {
		URI swiftUrl = keystoneAuthentication.getSwiftUrl();

		if (swiftHttpClient == null) {
			swiftHttpClient = openstackClientPool.httpClientPool
					.getClient(swiftUrl);
		}

		if (!swiftHttpClient.isConnected()) {
			return swiftHttpClient.connect();
		}

		return null;
	}

	public ChannelFuture readObject(String path, ObjectDataSink listener) {
		ChannelFuture future = authenticate();
		if (future != null)
			return future;

		future = connectToSwift();
		if (future != null)
			return future;

		if (httpListener == null) {
			URI swiftUrl = keystoneAuthentication.getSwiftUrl();
			String fullPath = swiftUrl.getPath();
			fullPath += "/" + path;
			HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
					HttpMethod.GET, fullPath);
			request.setHeader(HttpHeaders.Names.HOST, HttpClientConnection.getHostHeader(swiftUrl));
			request.setHeader("X-Auth-Token", keystoneAuthentication.getTokenId());
			request.setHeader(HttpHeaders.Names.USER_AGENT, "Java Proxy");
			

			// TODO: Use keep-alive
//			request.setHeader(HttpHeaders.Names.CONNECTION,
//					HttpHeaders.Values.KEEP_ALIVE);

			httpListener = new ObjectListener(swiftHttpClient.getChannel(), listener);
			swiftHttpClient.doRequest(request, httpListener);

			return httpListener.getFuture();
		}

		return null;
	}
}
