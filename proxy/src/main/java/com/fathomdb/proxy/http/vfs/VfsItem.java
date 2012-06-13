package com.fathomdb.proxy.http.vfs;

import java.util.Date;
import java.util.Map;

import org.openstack.crypto.ByteString;

import com.fathomdb.meta.Meta;
import com.fathomdb.proxy.http.handlers.ContentType;
import com.fathomdb.proxy.protobuf.ModelsProtobuf.VfsItemModel;
import com.fathomdb.proxy.protobuf.ModelsProtobuf.VfsItemModel.WellKnownContentType;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;

public class VfsItem {
	static final Meta<VfsItem> META = Meta.get(VfsItem.class);

	private final String name;
	private final ByteString contentHash;
	private final long length;
	private final ContentType contentType;
	private final long lastModified;

	public final Map<String, VfsItem> children = Maps.newHashMap();

	public VfsItem(String name, ByteString contentHash, long length, ContentType contentType, long lastModified) {
		this.name = name;
		this.contentHash = contentHash;
		this.length = length;
		this.contentType = contentType;
		this.lastModified = lastModified;
	}

	public VfsItem(String name) {
		this(name, null, -1, null, 0);
	}

	public static VfsItem map(VfsItemModel model) {
		// We pay the price of copying to try to maximize memory efficiency
		String name = model.getName();
		ByteString contentHash = null;
		com.google.protobuf.ByteString v = model.getHash();
		if (v != null) {
			contentHash = new ByteString(v.toByteArray());
		}

		long length = -1;
		if (model.hasLength()) {
			length = model.getLength();
		}

		ContentType contentType = null;
		if (model.hasContentType()) {
			String s = model.getContentType();
			contentType = ContentType.get(s);
		} else if (model.hasWellKnownContentType()) {
			WellKnownContentType wellKnownContentType = model.getWellKnownContentType();
			switch (wellKnownContentType) {
			case DIRECTORY:
				contentType = ContentType.TYPE_DIRECTORY;
				break;
			default:
				throw new IllegalStateException();
			}
		}

		long lastModified = -1;
		if (model.hasLastModified()) {
			lastModified = model.getLastModified();
		}

		VfsItem item = new VfsItem(name, contentHash, length, contentType, lastModified);

		for (VfsItemModel childModel : model.getChildList()) {
			VfsItem child = map(childModel);
			item.children.put(child.getName(), child);
		}

		return item;
	}

	public VfsItem getChild(String key) {
		return children.get(key);
	}

	public boolean isDirectory() {
		if (length > 0) {
			return false;
		}

		if (contentType == null || Objects.equal(contentType, ContentType.TYPE_X_DIRECTORY)
				|| Objects.equal(contentType, ContentType.TYPE_DIRECTORY)) {
			return true;
		}
		return false;
	}

	public String getName() {
		return name;
	}

	public ContentType getContentType() {
		return contentType;
	}

	public ByteString getContentHash() {
		return contentHash;
	}

	public Date getLastModified() {
		if (lastModified == 0) {
			return null;
		}

		return new Date(lastModified);
	}

	public VfsItem findChild(String name) {
		return children.get(name);
	}

	@Override
	public String toString() {
		return META.toString();
	}
}
