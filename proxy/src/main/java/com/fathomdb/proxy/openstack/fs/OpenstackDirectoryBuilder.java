package com.fathomdb.proxy.openstack.fs;

import org.openstack.crypto.ByteString;

import com.fathomdb.proxy.http.handlers.ContentType;
import com.fathomdb.proxy.http.vfs.VfsItem;
import com.fathomdb.proxy.openstack.ObjectMetadataListener;
import com.google.common.base.Splitter;

public class OpenstackDirectoryBuilder implements ObjectMetadataListener {

	final VfsItem root = new VfsItem(null);

	@Override
	public void gotObjectDetails(String objectName, byte[] objectHash, long objectBytes, String objectContentType,
			long objectLastModified) {
		VfsItem current = root;

		String fileName = null;

		for (String pathItem : Splitter.on('/').split(objectName)) {
			if (fileName == null) {
				fileName = pathItem;
			} else {
				// Turns out that wasn't the file name last time...
				VfsItem child = current.children.get(fileName);
				if (child == null) {
					child = new VfsItem(fileName);
					current.children.put(fileName, child);
				}
				current = child;
				fileName = pathItem;
			}
		}

		ContentType contentType = ContentType.get(objectContentType);
		VfsItem item = new VfsItem(fileName, new ByteString(objectHash), objectBytes, contentType, objectLastModified);
		current.children.put(fileName, item);
	}

	@Override
	public Object endObjects() {
		return getRoot();

	}

	public VfsItem getRoot() {
		return root;
	}

}
