package com.fathomdb.proxy.openstack;

public class DebugObjectMetadataListener implements ObjectMetadataListener {

	@Override
	public void gotObjectDetails(String objectName, String objectHash,
			long objectBytes, String objectContentType,
			String objectLastModified) {
		// )
		// OpenstackFile item = new OpenstackFile(items.add(item);
		System.out.println("Got object details: " + objectName + " "
				+ objectHash + " " + objectBytes + " " + objectContentType
				+ " " + objectLastModified);
	}

	@Override
	public Object endObjects() {
		System.out.println("No more results");
		return null;
	}

}
