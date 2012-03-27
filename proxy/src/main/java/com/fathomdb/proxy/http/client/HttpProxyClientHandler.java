package com.fathomdb.proxy.http.client;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;

public class HttpProxyClientHandler extends SimpleChannelUpstreamHandler {
	static final Logger log = Logger.getLogger(HttpProxyClientHandler.class);

	private boolean readingChunks;
	private HttpResponse chunkedResponse;

	private HttpResponseHandler target;

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		if (!readingChunks) {
			HttpResponse response = (HttpResponse) e.getMessage();

			// System.out.println("STATUS: " + response.getStatus());
			// System.out.println("VERSION: " + response.getProtocolVersion());
			// System.out.println();

			// if (!response.getHeaderNames().isEmpty()) {
			// for (String name : response.getHeaderNames()) {
			// for (String value : response.getHeaders(name)) {
			// System.out.println("HEADER: " + name + " = " + value);
			// }
			// }
			// System.out.println();
			// }

			if (response.isChunked()) {
				readingChunks = true;
				chunkedResponse = response;
				boolean isLast = false;
				target.gotData(response, null, isLast);

				// System.out.println("CHUNKED CONTENT {");
			} else {
				boolean isLast = true;
				target.gotData(response, null, isLast);

				// ChannelBuffer content = response.getContent();
				// if (content.readable()) {
				// System.out.println("CONTENT {");
				// System.out.println(content.toString(CharsetUtil.UTF_8));
				// System.out.println("} END OF CONTENT");
				// }
			}
		} else {
			HttpChunk chunk = (HttpChunk) e.getMessage();

			// log.debug("Got chunk " + chunk);

			boolean isLast = chunk.isLast();
			target.gotData(chunkedResponse, chunk, isLast);

			if (isLast) {
				readingChunks = false;
				chunkedResponse = null;
			}

			// if (chunk.isLast()) {
			// readingChunks = false;
			// System.out.println("} END OF CHUNKED CONTENT");
			// } else {
			// System.out
			// .print(chunk.getContent().toString(CharsetUtil.UTF_8));
			// System.out.flush();
			// }
		}
	}

	public void setTarget(HttpResponseHandler target) {
		this.target = target;
	}
}