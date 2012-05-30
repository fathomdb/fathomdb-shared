package com.fathomdb.cache;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class CacheFile implements Cache {
	static final Logger log = LoggerFactory.getLogger(CacheFile.class);

	final File file;
	final MappedByteBuffer buffer;

	// TODO: Replace with a memory efficient map?
	final Map<HashKey, CacheFileEntry> entries;
	final ZoneAllocator freeList;

	static final int VERSION = 1;

	// TODO: Remove this limitation ?
	static final int RESERVED_HEADER_SIZE = 32 * 1024 * 1024;
	static final int FILE_LENGTH = 1024 * 1024 * 1024;

	boolean isDirty;

	static class LockCollection {
		// TODO: Move to a highly concurrent algorithm here ??

		// TODO: Work with the garbage collector...
		// just have a pointer to a generation, and then use a finalizer to
		// determine when there are no more references to that generation
		// Or maybe we could save multiple versions of the dictionary,
		// and keep track of when there are no old references
		final Set<CacheLock> locks = Sets.newHashSet();

		void add(CacheLock lock) {
			synchronized (locks) {
				locks.add(lock);
			}
		}

		void remove(CacheLock lock) {
			synchronized (locks) {
				locks.remove(lock);
			}
		}
	}

	final LockCollection locks = new LockCollection();

	static class CacheFileEntry {
		final HashKey key;

		int position;
		int length;

		CacheFileEntry(HashKey key, int position, int length) {
			this.key = key;
			this.position = position;
			this.length = length;
		}

	}

	private CacheFile(File file, MappedByteBuffer buffer) {
		this.file = file;
		this.buffer = buffer;
		EntryData metadata = readMetadata(buffer);
		this.entries = metadata.entries;
		this.freeList = ZoneAllocator.buildFreeList(metadata.entries.values(), metadata.dataAreaStart, buffer.limit());
	}

	public class CacheLock implements Closeable {
		final ByteBuffer buffer;
		final CacheFileEntry entry;

		public CacheLock(ByteBuffer buffer, CacheFileEntry entry) {
			this.buffer = buffer;
			this.entry = entry;
		}

		@Override
		public void close() {
			locks.remove(this);
		}

		public ByteBuffer getBuffer() {
			return buffer;
		}
	}

	CacheLock readEntry(CacheFileEntry entry) {
		ByteBuffer ret = buffer.duplicate();
		ret.position(entry.position);
		ret.limit(entry.position + entry.length);

		// Do we need to call slice??
		// ret = ret.slice();

		// TODO: Validate slice

		CacheLock lock = new CacheLock(ret, entry);
		locks.add(lock);
		return lock;
	}

	@Override
	public CacheLock lookup(HashKey key) {
		CacheFileEntry entry;
		synchronized (entries) {
			entry = entries.get(key);
		}
		if (entry == null) {
			return null;
		}

		return readEntry(entry);
	}

	public class Allocation {
		public final ByteBuffer buffer;
		final int position;
		final int length;

		private Allocation(ByteBuffer buffer, int position, int length) {
			this.buffer = buffer;
			this.position = position;
			this.length = length;
		}

		CacheFile getCacheFile() {
			return CacheFile.this;
		}

	}

	@Override
	public Allocation allocate(int dataSize) {
		int position = freeList.allocate(dataSize);
		if (position <= 0) {
			return null;
		}

		ByteBuffer allocated = buffer.duplicate();
		allocated.position(position);
		allocated.limit(position + dataSize);
		allocated = allocated.slice();

		return new Allocation(allocated, position, dataSize);
	}

	@Override
	public void store(HashKey key, Allocation allocation) {
		if (this != allocation.getCacheFile()) {
			throw new IllegalArgumentException();
		}

		// TODO: Header etc?
		CacheFileEntry entry = new CacheFileEntry(key, allocation.position, allocation.length);
		synchronized (entries) {
			isDirty = true;
			entries.put(key, entry);
		}
	}

	static class EntryData {
		public Map<HashKey, CacheFileEntry> entries;
		public int dataAreaStart;
	}

	static EntryData readMetadata(ByteBuffer buffer) {
		buffer.rewind();

		int version = buffer.getInt();
		StrongHash hash = StrongHash.read(buffer);
		int entryCount = buffer.getInt();
		int payloadLength = buffer.getInt();

		if (version != VERSION) {
			throw new IllegalStateException("Invalid version number");
		}

		if (payloadLength < 0) {
			throw new IllegalStateException("Invalid payloadLength");
		}

		if (entryCount < 0) {
			throw new IllegalStateException("Invalid entryCount");
		}

		int payloadStart = buffer.position();
		if ((payloadStart + payloadLength + StrongHash.SIZE) > buffer.limit()) {
			throw new IllegalStateException("Invalid header length");
		}

		buffer.position(payloadStart + payloadLength);
		StrongHash hash2 = StrongHash.read(buffer);

		if (!hash.equals(hash2)) {
			throw new IllegalStateException("Different hash values stored");
		}

		buffer.rewind();

		ByteBuffer metadata = buffer.duplicate();
		metadata.position(payloadStart);
		metadata.limit(payloadStart + payloadLength);
		metadata = metadata.slice();

		int hashCapacity = Math.min(1024, entryCount + entryCount / 4);
		Map<HashKey, CacheFileEntry> entries = new HashMap<HashKey, CacheFileEntry>(hashCapacity);

		metadata.rewind();
		StrongHash actual = StrongHash.computeHash(metadata);
		if (!hash.equals(actual)) {
			throw new IllegalStateException("Corrupt segment: data did not match hash");
		}

		metadata.rewind();
		for (int i = 0; i < entryCount; i++) {
			HashKey key = HashKey.get(metadata);

			int position = metadata.getInt();
			int length = metadata.getInt();

			if (position <= 0 || length <= 0) {
				throw new IllegalStateException("Corrupt position/length in entry");
			}

			CacheFileEntry entry = new CacheFileEntry(key, position, length);
			entries.put(key, entry);
		}

		if (metadata.position() != metadata.limit()) {
			throw new IllegalStateException("Extra data left");
		}

		EntryData entryData = new EntryData();
		entryData.entries = entries;
		entryData.dataAreaStart = 32 * 1024 * 1024;
		return entryData;
	}

	public static void create(File file) {
		try {
			RandomAccessFile randomFile = new RandomAccessFile(file, "rw");
			randomFile.setLength(FILE_LENGTH);
			FileChannel rwChannel = randomFile.getChannel();
			MappedByteBuffer buffer = rwChannel.map(FileChannel.MapMode.READ_WRITE, 0, (int) rwChannel.size());
			writeMetadata(Collections.<CacheFileEntry> emptyList(), buffer);
		} catch (IOException e) {
			throw new IllegalStateException("Error creating empty caching file", e);
		}
	}

	public static CacheFile open(File file) {
		return open(file, true);
	}

	public static CacheFile open(File file, boolean removeIfCorrupt) {
		if (!file.exists()) {
			log.info("Cache file does not exist, creating: " + file);
			create(file);
		}

		try {
			FileChannel rwChannel = new RandomAccessFile(file, "rw").getChannel();
			MappedByteBuffer buffer = rwChannel.map(FileChannel.MapMode.READ_WRITE, 0, (int) rwChannel.size());

			return new CacheFile(file, buffer);
		} catch (Exception e) {
			log.warn("Error opening cache file: " + file, e);

			if (removeIfCorrupt) {
				log.warn("Moving cache file: " + file, e);
				file.renameTo(new File(file.getAbsolutePath() + "." + System.currentTimeMillis()));
				return open(file, false);
			} else {
				throw new IllegalStateException("Error opening cache file", e);
			}
		}

	}

	public void writeMetadata() {
		List<CacheFileEntry> snapshot;
		synchronized (entries) {
			if (!isDirty) {
				log.debug("Metadata not dirty, won't write");
				return;
			}

			isDirty = false;

			snapshot = Lists.newArrayList(entries.values());
		}

		log.info("Writing metadata snapshot");

		writeMetadata(snapshot, buffer);
	}

	static void writeMetadata(List<CacheFileEntry> entries, ByteBuffer buffer) {
		// TODO: Write this to a separate file, as a backup??
		// Or just keep it in a separate file so we can flush it separately

		StrongHash hash = StrongHash.ZERO;

		buffer.rewind();

		buffer.putInt(0);
		hash.put(buffer);
		buffer.putInt(entries.size());
		buffer.putInt(0);

		int payloadStart = buffer.position();

		for (CacheFileEntry entry : entries) {
			HashKey key = entry.key;
			key.put(buffer);
			buffer.putInt(entry.position);
			buffer.putInt(entry.length);
		}

		int payloadEnd = buffer.position();
		hash.put(buffer);

		if (buffer.position() > RESERVED_HEADER_SIZE) {
			throw new IllegalStateException();
		}

		ByteBuffer payloadBuffer = buffer.duplicate();
		payloadBuffer.position(payloadStart);
		payloadBuffer.limit(payloadEnd);

		hash = StrongHash.computeHash(payloadBuffer);

		buffer.position(payloadEnd);
		hash.put(buffer);

		buffer.position(0);
		buffer.putInt(VERSION);
		hash.put(buffer);
		buffer.putInt(entries.size());
		buffer.putInt(payloadEnd - payloadStart);
	}

	public void forceToDisk() {
		buffer.force();
	}

	@Override
	public String toString() {
		return "CacheFile: file=" + file + " entryCount=" + entries.size();
	}

}
