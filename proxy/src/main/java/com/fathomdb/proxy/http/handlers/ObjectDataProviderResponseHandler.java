package com.fathomdb.proxy.http.handlers;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import com.fathomdb.proxy.http.handlers.RequestHandler;
import com.fathomdb.proxy.http.server.GenericRequest;
import com.fathomdb.proxy.objectdata.ObjectDataProvider;
import com.fathomdb.proxy.openstack.Futures;

public class ObjectDataProviderResponseHandler implements RequestHandler {
	static final Logger log = Logger
			.getLogger(ObjectDataProviderResponseHandler.class);

	final ObjectDataProvider provider;

	public ObjectDataProviderResponseHandler(ObjectDataProvider provider) {
		this.provider = provider;
	}

	@Override
	public ChannelFuture handleRequest(GenericRequest request) {
		Channel channel = request.getChannel();
		final SendObjectDataResponse response = new SendObjectDataResponse(
				channel);

		ChannelFuture future = provider.handle(request, response);
		boolean cancellable = false;
		return Futures.on(channel, cancellable).then(future)
				.then(response.getFuture());
	}

}
