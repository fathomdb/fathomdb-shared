//package com.fathomdb.proxy.http.handlers;
//
//import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
//import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map.Entry;
//import java.util.Set;
//
//import org.apache.log4j.Logger;
//import org.jboss.netty.channel.ChannelFuture;
//import org.jboss.netty.channel.ChannelFutureListener;
//import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
//import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
//import org.jboss.netty.handler.codec.http.HttpHeaders;
//import org.jboss.netty.handler.codec.http.HttpMethod;
//import org.jboss.netty.handler.codec.http.HttpRequest;
//import org.jboss.netty.handler.codec.http.HttpResponse;
//import org.jboss.netty.handler.codec.http.HttpVersion;
//import com.fathomdb.proxy.http.HttpScheme;
//import com.fathomdb.proxy.http.client.HttpClientPool;
//import com.fathomdb.proxy.http.client.HttpClientConnection;
//import com.fathomdb.proxy.http.server.GenericRequest;
//
//public class ProxyRequestHandler implements RequestHandler {
//	static final Logger log = Logger.getLogger(ProxyRequestHandler.class);
//
//	final HttpClientPool httpClientPool;
//
//	public ProxyRequestHandler(HttpClientPool httpClientPool) {
//		this.httpClientPool = httpClientPool;
//	}
//
//	@Override
//	public ChannelFuture handleRequest(GenericRequest request) {
//		final String requestURI = request.getRequestURI();
//		String hostAndPort = request.getHeader("Host");
//		HttpMethod method = request.getMethod();
//
//		log.debug("HandleRequest " + method + " " + hostAndPort + " "
//				+ requestURI);
//
//		// httpClientPool.getClient();
//
//		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
//		String responseBody = "Hello world " + method + " " + hostAndPort + " "
//				+ requestURI;
//
//		int port = 0;
//
//		int colonIndex = hostAndPort.indexOf(':');
//		if (colonIndex != -1) {
//		} else {
//		}
//
//		// TODO: Configurable
//		HttpScheme scheme = HttpScheme.HTTP;
//		hostAndPort = "www.google.com";
//
//		HttpRequest upstreamRequest = null;
//
//		if (method.equals(HttpMethod.GET)) {
//			// Prepare the HTTP request.
//			upstreamRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
//					HttpMethod.GET, requestURI);
//			upstreamRequest.setHeader(HttpHeaders.Names.HOST, hostAndPort);
//
//			// TODO: Use keep-alive
//			upstreamRequest.setHeader(HttpHeaders.Names.CONNECTION,
//					HttpHeaders.Values.CLOSE);
//
//			upstreamRequest.setHeader(HttpHeaders.Names.ACCEPT_ENCODING,
//					HttpHeaders.Values.GZIP);
//
//			List<Entry<String, String>> headers = request.getHeaders();
//
//			Set<String> stripHeaders = new HashSet<String>();
//			stripHeaders.add("host");
//			stripHeaders.add("connection");
//			stripHeaders.add("referer");
//
//			for (Entry<String, String> header : headers) {
//				String headerName = header.getKey();
//				System.out.println(headerName + ": " + header.getValue());
//
//				if (stripHeaders.contains(headerName.toLowerCase())) {
//					System.out.println(headerName + "(SKIP)");
//					continue;
//
//				}
//
//				upstreamRequest.addHeader(headerName, header.getValue());
//			}
//		}
//
//		if (upstreamRequest == null) {
//			// TODO: Send a nice error
//			throw new UnsupportedOperationException();
//		}
//
//		final HttpClientConnection connection = httpClientPool.getClient(
//				scheme, hostAndPort);
//
//		final RelayHttpResponseListener listener = new RelayHttpResponseListener(
//				request.getChannel());
//
//		ChannelFuture connectionFuture = connection.connect();
//		final HttpRequest sendRequest = upstreamRequest;
//		connectionFuture.addListener(new ChannelFutureListener() {
//			@Override
//			public void operationComplete(ChannelFuture future)
//					throws Exception {
//				if (future.isSuccess()) {
//					log.debug("Connection completed with success");
//					connection.doRequest(sendRequest, listener);
//				} else {
//					log.debug("Connection failed");
//					listener.setFailure(future.getCause());
//				}
//			}
//		});
//
//		return listener.getFuture();
//
//		// Encode the cookie.
//		// String cookieString = request.getHeader(COOKIE);
//		// if (cookieString != null) {
//		// CookieDecoder cookieDecoder = new CookieDecoder();
//		// Set<Cookie> cookies = cookieDecoder.decode(cookieString);
//		// if (!cookies.isEmpty()) {
//		// // Reset the cookies if necessary.
//		// CookieEncoder cookieEncoder = new CookieEncoder(true);
//		// for (Cookie cookie : cookies) {
//		// cookieEncoder.addCookie(cookie);
//		// }
//		// response.addHeader(SET_COOKIE, cookieEncoder.encode());
//		// }
//		// }
//	}
//
//}
