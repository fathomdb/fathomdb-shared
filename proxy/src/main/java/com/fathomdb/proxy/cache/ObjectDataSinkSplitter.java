package com.fathomdb.proxy.cache;

import org.jboss.netty.buffer.ChannelBuffer;

import com.fathomdb.proxy.objectdata.ObjectDataSink;

public class ObjectDataSinkSplitter implements ObjectDataSink {
	final ObjectDataSink[] children;

	public ObjectDataSinkSplitter(ObjectDataSink... children) {
		this.children = children;
	}

	@Override
	public void gotData(ChannelBuffer content) {
		for (int i = 0; i < children.length; i++) {
			ChannelBuffer copy = content;
			if ((i + 1) != children.length) {
				copy = copy.duplicate();
			}

			children[i].gotData(copy);
		}
	}

	@Override
	public void beginData(long contentLength) {
		for (ObjectDataSink child : children)
			child.beginData(contentLength);
	}

	@Override
	public void endData() {
		for (ObjectDataSink child : children)
			child.endData();
	}

}
