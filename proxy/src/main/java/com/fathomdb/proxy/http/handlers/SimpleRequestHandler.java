package com.fathomdb.proxy.http.handlers;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;

import com.fathomdb.proxy.http.server.GenericRequest;

public class SimpleRequestHandler implements RequestHandler {

	@Override
	public ChannelFuture handleRequest(GenericRequest request) {
		return writeResponse(request);

	}

	ChannelFuture writeResponse(GenericRequest request) {
		// Decide whether to close the connection or not.

		// Build the response object.
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
		String responseBody = "Hello world";
		response.setContent(ChannelBuffers.copiedBuffer(responseBody, CharsetUtil.UTF_8));
		response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");

		if (request.isKeepAlive()) {
			// Add 'Content-Length' header only for a keep-alive connection.
			response.setHeader(CONTENT_LENGTH, response.getContent().readableBytes());
		}

		// Encode the cookie.
		// String cookieString = request.getHeader(COOKIE);
		// if (cookieString != null) {
		// CookieDecoder cookieDecoder = new CookieDecoder();
		// Set<Cookie> cookies = cookieDecoder.decode(cookieString);
		// if (!cookies.isEmpty()) {
		// // Reset the cookies if necessary.
		// CookieEncoder cookieEncoder = new CookieEncoder(true);
		// for (Cookie cookie : cookies) {
		// cookieEncoder.addCookie(cookie);
		// }
		// response.addHeader(SET_COOKIE, cookieEncoder.encode());
		// }
		// }

		// Write the response.
		return request.getChannel().write(response);
	}

}
