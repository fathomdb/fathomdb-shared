package com.fathomdb.proxy.objectdata;

import java.util.Set;
import java.util.UUID;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultCookie;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

import com.fathomdb.proxy.http.server.GenericRequest;

public class StandardResponses {
	public static HttpResponse buildErrorResponse(GenericRequest request, HttpResponseStatus status) {
		HttpResponse response = buildResponse(request, status);

		// response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, 0);

		String responseBody = "Error " + status;
		response.setContent(ChannelBuffers.copiedBuffer(responseBody, CharsetUtil.UTF_8));
		response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
		response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, response.getContent().readableBytes());

		return response;
	}

	static final int SECONDS_IN_MINUTE = 60;
	static final int SECONDS_IN_HOUR = SECONDS_IN_MINUTE * 60;
	static final int SECONDS_IN_DAY = SECONDS_IN_HOUR * 24;
	static final int SECONDS_IN_YEAR = SECONDS_IN_DAY * 365;
	static final int SECONDS_IN_DECADE = 10 * SECONDS_IN_YEAR;

	public static HttpResponse buildResponse(GenericRequest request, HttpResponseStatus status) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);

		// Use cookies to allow tracking sessions in the debug log
		String cookieName = "uuid";
		boolean found = false;
		String cookieString = request.getHeader(HttpHeaders.Names.COOKIE);
		if (cookieString != null) {
			CookieDecoder cookieDecoder = new CookieDecoder();
			Set<Cookie> cookies = cookieDecoder.decode(cookieString);
			if (!cookies.isEmpty()) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals(cookieName)) {
						found = true;
					}
				}
			}
		}

		if (!found) {
			CookieEncoder cookieEncoder = new CookieEncoder(true);
			Cookie cookie = new DefaultCookie(cookieName, UUID.randomUUID().toString());
			cookie.setMaxAge(SECONDS_IN_DECADE);

			cookieEncoder.addCookie(cookie);
			response.addHeader(HttpHeaders.Names.SET_COOKIE, cookieEncoder.encode());
		}

		return response;
	}

}
