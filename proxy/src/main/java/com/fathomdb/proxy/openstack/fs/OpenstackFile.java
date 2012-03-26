package com.fathomdb.proxy.openstack.fs;

public class OpenstackFile extends OpenstackItem {

	private final String name;
	private final String hash;
	private final long length;
	private final String contentType;
	private final String lastModified;

	public OpenstackFile(String name, String hash, long length,
			String contentType, String lastModified) {
		this.name = name;
		this.hash = hash;
		this.length = length;
		this.contentType = contentType;
		this.lastModified = lastModified;
	}

	@Override
	public boolean isFile() {
		return true;
	}

}
