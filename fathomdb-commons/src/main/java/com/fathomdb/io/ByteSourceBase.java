package com.fathomdb.io;


public abstract class ByteSourceBase implements ByteSource {
	ByteMetadata metadata;

	@Override
	public ByteMetadata getMetadata() {
		if (metadata == null) {
			metadata = new ByteMetadata(this);
		}
		return metadata;
	}

}
