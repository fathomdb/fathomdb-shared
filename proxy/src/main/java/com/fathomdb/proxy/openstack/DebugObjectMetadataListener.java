package com.fathomdb.proxy.openstack;

import com.fathomdb.proxy.utils.Hex;

public class DebugObjectMetadataListener implements ObjectMetadataListener {

	@Override
	public void gotObjectDetails(String objectName, byte[] objectHash,
			long objectBytes, String objectContentType,
			long objectLastModified) {
		// )
		// OpenstackFile item = new OpenstackFile(items.add(item);
		System.out.println("Got object details: " + objectName + " "
				+ Hex.toHex(objectHash) + " " + objectBytes + " " + objectContentType
				+ " " + objectLastModified);
	}

	@Override
	public Object endObjects() {
		System.out.println("No more results");
		return null;
	}

}
