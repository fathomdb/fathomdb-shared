package com.fathomdb.proxy.cache;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.fathomdb.proxy.objectdata.ObjectDataSink;

public class ObjectDataSinkSplitter implements ObjectDataSink {
	final ObjectDataSink[] children;

	public ObjectDataSinkSplitter(ObjectDataSink... children) {
		this.children = children;
	}

	@Override
	public void gotData(ChannelBuffer content, boolean isLast) {
		for (int i = 0; i < children.length; i++) {
			ChannelBuffer copy = content;
			if ((i + 1) != children.length) {
				copy = copy.duplicate();
			}

			children[i].gotData(copy, isLast);
		}
	}

	@Override
	public void endData() {
		for (ObjectDataSink child : children)
			child.endData();
	}

	@Override
	public void beginResponse(HttpResponse response) {
		for (ObjectDataSink child : children)
			child.beginResponse(response);
	}

}
