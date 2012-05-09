package com.fathomdb.dns.server;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

public class DnsServerPipelineFactory implements ChannelPipelineFactory {

	final RecordProvider recordProvider;

	final DnsMessageDecoder decoder;
	final DnsMessageHandler handler;

	public DnsServerPipelineFactory(RecordProvider recordProvider) {
		this.recordProvider = recordProvider;
		this.decoder = new DnsMessageDecoder();
		this.handler = new DnsMessageHandler(recordProvider);

	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline result = Channels.pipeline();
		result.addLast("decoder", this.decoder);
		result.addLast("handler", this.handler);
		return result;
	}

}