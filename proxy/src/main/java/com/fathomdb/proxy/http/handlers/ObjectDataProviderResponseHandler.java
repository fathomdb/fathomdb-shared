package com.fathomdb.proxy.http.handlers;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import com.fathomdb.proxy.http.handlers.RequestHandler;
import com.fathomdb.proxy.http.logger.RequestLogger;
import com.fathomdb.proxy.http.server.GenericRequest;
import com.fathomdb.proxy.objectdata.ObjectDataProvider;
import com.fathomdb.proxy.objectdata.ObjectDataProvider.Handler;
import com.fathomdb.proxy.openstack.EasyAsync;

public class ObjectDataProviderResponseHandler implements RequestHandler {
	static final Logger log = Logger
			.getLogger(ObjectDataProviderResponseHandler.class);

	final ObjectDataProvider provider;

	final RequestLogger logger;

	public ObjectDataProviderResponseHandler(RequestLogger logger,
			ObjectDataProvider provider) {
		this.logger = logger;
		this.provider = provider;
	}

	@Override
	public ChannelFuture handleRequest(final GenericRequest request) {
		Channel channel = request.getChannel();
		final SendObjectDataResponse response = new SendObjectDataResponse(
				request, logger, channel);

		final Handler handler = provider.buildHandler(request);

		ChannelFuture channelFuture = new EasyAsync(channel, false) {
			@Override
			protected void poll() throws Exception {
				handler.handle(response);
			}
		}.start();

		channelFuture.addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture future)
					throws Exception {
				try {
					handler.close();
				} catch (Exception e) {
					log.warn("Ignoring error while closing handler", e);
				}
			}
		});

		return channelFuture;
	}

}
