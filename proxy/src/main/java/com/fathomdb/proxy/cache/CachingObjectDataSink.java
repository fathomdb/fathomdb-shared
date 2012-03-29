package com.fathomdb.proxy.cache;

import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.fathomdb.proxy.cache.CacheFile.Allocation;
import com.fathomdb.proxy.objectdata.ObjectDataSink;

public class CachingObjectDataSink implements ObjectDataSink {
	final CacheFile cache;
	private Allocation allocation;
	private final HashKey cacheKey;

	public CachingObjectDataSink(CacheFile cache, HashKey cacheKey) {
		this.cache = cache;
		this.cacheKey = cacheKey;
	}

	@Override
	public void gotData(ChannelBuffer content, boolean isLast) {
		if (allocation != null) {
			// This is a potentially blocking operation
			// So... make sure that mmap is fast
			// i.e. either don't over allocate RAM, or have fast disks
			// also we probably want more threads that there are cores
			// If we did this asynchronously, we'd have to deal with the buffers
			// overflowing
			// The one thing we could do is dynamically tweak the cache policy;
			// dialing it down when I/O is the bottleneck
			int chunkSize = content.readableBytes();

			ByteBuffer allocationBuffer = allocation.buffer;
			int oldLimit = allocationBuffer.limit();
			int startPosition = allocationBuffer.position();
			allocationBuffer.limit(startPosition + chunkSize);
			content.readBytes(allocationBuffer);
			allocationBuffer.limit(oldLimit);
//			allocationBuffer.position(startPosition + chunkSize);
		}
	}

	@Override
	public void beginResponse(HttpResponse response) {
		long contentLength = HttpHeaders.getContentLength(response, -1);

		if (contentLength == -1) {
			// Unknown content length
		} else {
			if (contentLength < Integer.MAX_VALUE) {
				Allocation allocation = cache.allocate((int) contentLength);
				if (allocation != null) {
					this.allocation = allocation;
				}
			}
		}
	}

	@Override
	public void endData() {
		if (allocation != null) {
			cache.store(cacheKey, allocation);
		}
	}

}
