package com.fathomdb.proxy.openstack;

public interface ObjectMetadataListener {

	void gotObjectDetails(String objectName, String objectHash,
			long objectBytes, String objectContentType,
			String objectLastModified);

	Object endObjects();

}
