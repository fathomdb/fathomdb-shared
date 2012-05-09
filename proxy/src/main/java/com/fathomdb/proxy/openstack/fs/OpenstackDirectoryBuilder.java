package com.fathomdb.proxy.openstack.fs;

import com.fathomdb.proxy.cache.HashKey;
import com.fathomdb.proxy.http.handlers.ContentType;
import com.fathomdb.proxy.openstack.ObjectMetadataListener;
import com.google.common.base.Splitter;

public class OpenstackDirectoryBuilder implements ObjectMetadataListener {

	final OpenstackItem root = new OpenstackItem(null);

	@Override
	public void gotObjectDetails(String objectName, byte[] objectHash,
			long objectBytes, String objectContentType,
			long objectLastModified) {
		OpenstackItem current = root;

		String fileName = null;

		for (String pathItem : Splitter.on('/').split(objectName)) {
			if (fileName == null) {
				fileName = pathItem;
			} else {
				// Turns out that wasn't the file name last time...
				OpenstackItem child = current.children.get(fileName);
				if (child == null) {
					child = new OpenstackItem(fileName);
					current.children.put(fileName, child);
				}
				current = child;
				fileName = pathItem;
			}
		}

		ContentType contentType = ContentType.get(objectContentType);
		OpenstackItem item = new OpenstackItem(fileName, new HashKey(objectHash),
				objectBytes, contentType, objectLastModified);
		current.children.put(fileName, item);
	}

	@Override
	public Object endObjects() {
		return getRoot();

	}

	public OpenstackItem getRoot() {
		return root;
	}

}
