package com.fathomdb.dns.server;

import java.net.Socket;
import java.net.SocketAddress;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DnsMessageHandler extends SimpleChannelUpstreamHandler {
	static final Logger log = LoggerFactory.getLogger(DnsMessageHandler.class);

	final RecordProvider recordProvider;

	public DnsMessageHandler(RecordProvider recordProvider) {
		this.recordProvider = recordProvider;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		DnsMessage query = (DnsMessage) e.getMessage();
		SocketAddress remoteAddress = e.getRemoteAddress();

		byte[] data = query.getOriginalMessageData();

		Socket s = null;
		byte[] reply = recordProvider.generateReply(query.getMessage(), data,
				data.length, s);

		if (reply != null) {
			ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(reply);

			// TODO: Do we need to close the channel for TCP?
			e.getChannel().write(buffer, remoteAddress); // .addListener(ChannelFutureListener.CLOSE);
		}

		// TODO: When should we close a TCP channel?

		// ChannelFuture future = requestHandler.handleRequest(request);

		// // Close the non-keep-alive connection after the write operation is
		// // done.
		// if (!request.isKeepAlive()) {
		// log.debug("Request not keep-alive; closing");
		// future.addListener(ChannelFutureListener.CLOSE);
		// }
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		// TODO: Should we close a UDP channel?
		e.getCause().printStackTrace();
		e.getChannel().close();
	}
}