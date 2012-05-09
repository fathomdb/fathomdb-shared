package com.fathomdb.proxy.openstack.fs;

import java.util.Date;
import java.util.Map;

import com.fathomdb.meta.Meta;
import com.fathomdb.proxy.cache.HashKey;
import com.fathomdb.proxy.http.handlers.ContentType;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;

public class OpenstackItem {
	private final String name;
	private final HashKey contentHash;
	private final long length;
	private final ContentType contentType;
	private final long lastModified;

	final Map<String, OpenstackItem> children = Maps.newHashMap();

	static final Meta<OpenstackItem> META = Meta.get(OpenstackItem.class);

	public OpenstackItem(String name, HashKey contentHash, long length,
			ContentType contentType, long lastModified) {
		this.name = name;
		this.contentHash = contentHash;
		this.length = length;
		this.contentType = contentType;
		this.lastModified = lastModified;
	}

	public OpenstackItem(String name) {
		this(name, null, -1, null, 0);
	}

	public OpenstackItem getChild(String key) {
		return children.get(key);
	}
	
	static final ContentType TYPE_X_DIRECTORY = ContentType.get("application/x-directory");
	static final ContentType TYPE_DIRECTORY = ContentType.get("application/directory");
	

	public boolean isDirectory() {
		if (length > 0)
			return false;

		if (contentType == null
				|| Objects.equal(contentType,TYPE_X_DIRECTORY)
				|| Objects.equal(contentType, TYPE_DIRECTORY))
			return true;
		return false;
	}

	public ContentType getContentType() {
		return contentType;
	}

	public HashKey getContentHash() {
		return contentHash;
	}

	public Date getLastModified() {
		if (lastModified == 0)
			return null;

		return new Date(lastModified);
	}

	@Override
	public String toString() {
		return META.toString();
	}
}
