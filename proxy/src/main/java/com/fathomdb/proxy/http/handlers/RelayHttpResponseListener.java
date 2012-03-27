//package com.fathomdb.proxy.http.handlers;
//
//import org.apache.log4j.Logger;
//import org.jboss.netty.channel.Channel;
//import org.jboss.netty.channel.ChannelFuture;
//import org.jboss.netty.channel.ChannelFutureListener;
//import org.jboss.netty.handler.codec.http.HttpChunk;
//import org.jboss.netty.handler.codec.http.HttpResponse;
//
//import com.fathomdb.proxy.http.client.HttpResponseHandler;
//import com.fathomdb.proxy.http.client.TaskWithFuture;
//
//public class RelayHttpResponseListener extends TaskWithFuture implements
//		HttpResponseHandler {
//	static final Logger log = Logger.getLogger(RelayHttpResponseListener.class);
//
//	public RelayHttpResponseListener(Channel channel) {
//		super(channel);
//	}
//
//	@Override
//	public void gotData(HttpResponse response, HttpChunk chunk, boolean isLast) {
//		ChannelFuture writeFuture;
//
//		if (chunk == null) {
//			// if (!isLast)
//			// throw new IllegalStateException();
//
//			response.removeHeader("Connection");
//			// response.removeHeader("X-XSS-Protection");
//
//			// if
//			// (!response.containsHeader(HttpHeaders.Names.TRANSFER_ENCODING)) {
//			// log.info("Adding chunked encoding header");
//			// response.addHeader(HttpHeaders.Names.TRANSFER_ENCODING,
//			// HttpHeaders.Values.CHUNKED);
//			// }
//
//			log.debug("Forwarding response: " + response);
//
//			// List<Entry<String, String>> headers = response.getHeaders();
//			// for (Entry<String, String> header : headers) {
//			// String headerName = header.getKey();
//			// if (headerName.equals("Connection")) {
//			// headers.r
//			// }
//			// }
//
//			writeFuture = channel.write(response);
//		} else {
//			log.debug("Forwarding chunk: " + chunk);
//
//			writeFuture = channel.write(chunk);
//		}
//
//		if (isLast) {
//			log.debug("Last chunk; adding listener");
//
//			writeFuture.addListener(new ChannelFutureListener() {
//				@Override
//				public void operationComplete(ChannelFuture future)
//						throws Exception {
//					log.debug("Last chunk complete");
//					if (future.isSuccess()) {
//						log.debug("Last chunk success");
//						RelayHttpResponseListener.this.future.setSuccess();
//					} else {
//						log.debug("Last chunk failure");
//
//						Throwable cause = future.getCause();
//						RelayHttpResponseListener.this.future.setFailure(cause);
//					}
//				}
//			});
//		}
//
//		// response.setContent(ChannelBuffers.copiedBuffer(responseBody,
//		// CharsetUtil.UTF_8));
//		// response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
//		//
//		// if (request.isKeepAlive()) {
//		// // Add 'Content-Length' header only for a keep-alive connection.
//		// response.setHeader(CONTENT_LENGTH, response.getContent()
//		// .readableBytes());
//		// } // Write the response.
//		// return request.getChannel().write(response);
//	}
//
//}
