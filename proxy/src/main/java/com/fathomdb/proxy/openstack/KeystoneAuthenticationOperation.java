package com.fathomdb.proxy.openstack;

import java.net.URI;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.util.CharsetUtil;

import com.fathomdb.proxy.http.client.HttpClientConnection;

public class KeystoneAuthenticationOperation {
	final KeystoneClient client;
	final OpenstackCredentials credentials;

	public KeystoneAuthenticationOperation(KeystoneClient client, OpenstackCredentials credentials) {
		this.client = client;
		this.credentials = credentials;
	}

	KeystoneResponseListener keystoneAuthentication;

	public KeystoneResponseListener authenticate() {
		if (keystoneAuthentication == null || !keystoneAuthentication.isAuthenticated()) {
			HttpClientConnection connection = client.getHttpClientConnection();
			keystoneAuthentication = new KeystoneResponseListener(connection.getChannel());

			HttpRequest authRequest = client.buildRequest(HttpMethod.POST, credentials.getAuthUrl().getPath());

			// TODO: Use keep-alive
			authRequest.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);

			authRequest.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/xml");
			authRequest.setHeader(HttpHeaders.Names.ACCEPT, "application/xml");

			// We hard-code this for simplicity
			// TODO: Use XML writer so we get escaping
			String username = credentials.getUsername();
			String password = credentials.getPassword();
			String tenantName = credentials.getTenant();
			String postBody = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
					+ "<auth xmlns=\"http://docs.openstack.org/identity/api/v2.0\"";
			if (tenantName != null) {
				postBody += " tenantName=\"" + tenantName + "\">";
			} else {
				postBody += ">";
			}

			postBody += "<passwordCredentials username=\"" + username + "\" password=\"" + password + "\"/></auth>";

			ChannelBuffer contentBuffer = ChannelBuffers.copiedBuffer(postBody, CharsetUtil.UTF_8);
			authRequest.setContent(contentBuffer);

			authRequest.setHeader(HttpHeaders.Names.CONTENT_LENGTH, contentBuffer.readableBytes());
			connection.doRequest(authRequest, keystoneAuthentication);

			throw new AsyncFutureException(keystoneAuthentication.getFuture(), "Keystone authentication");
		}

		return keystoneAuthentication;
	}

	public URI getSwiftUrl() {
		return authenticate().getSwiftUrl();
	}

	public String getAuthTokenId() {
		return authenticate().getTokenId();
	}
}
