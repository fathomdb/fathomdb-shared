package com.fathomdb.dns.server;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@Sharable
public class DnsMessageDecoder extends OneToOneDecoder {
	static final Logger log = LoggerFactory.getLogger(DnsMessageDecoder.class);

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		if (!(msg instanceof ChannelBuffer)) {
			return msg;
		}

		try {
			ChannelBuffer channelBuffer = (ChannelBuffer) msg;

			// Note - it sort of sucks that we have to copy the data
			// Message is missing a (byte[], offset, length) constructor though,
			// anyway

			// TODO: Implement our own DNSInput?

			int length = channelBuffer.readableBytes();
			byte[] messageData = new byte[length];
			channelBuffer.readBytes(messageData);

			return new DnsMessage(messageData);
		} catch (Exception e) {
			log.warn("Error in DNS message decoder", e);
			return null;
		}
	}

}