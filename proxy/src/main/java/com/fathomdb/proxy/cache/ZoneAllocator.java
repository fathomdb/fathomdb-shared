package com.fathomdb.proxy.cache;

import java.util.List;

import org.apache.log4j.Logger;

import com.fathomdb.proxy.cache.CacheFile.CacheFileEntry;
import com.google.common.collect.Lists;

public class ZoneAllocator {
	static final Logger log = Logger.getLogger(ZoneAllocator.class);
	
	static final int FAIL = -1;

	static final int PAGE_SIZE = 4096;
	
	static class FreeRange {
		int start;
		int end;

		FreeRange(int start, int end) {
			super();
			this.start = start;
			this.end = end;
		}

		@Override
		public String toString() {
			return start + "-" + end;
		}

	}

	static class FreeList {
		final List<FreeRange> freeList = Lists.newArrayList();

		public void add(FreeRange range) {
			freeList.add(range);
		}

		public void remove(int start, int end) {

			FreeRange found = null;
			// TODO: Use binary search
			for (FreeRange free : freeList) {
				if (free.end < start)
					continue;
				if (free.start > end)
					continue;

				found = free;
				break;
			}

			if (found == null)
				throw new IllegalStateException();

			if (found.start < start || found.end < end)
				throw new IllegalStateException();

			// Truncate or split the range
			if (found.end == end) {
				found.end = start;
			} else if (found.start == start) {
				found.start = end;
			} else {
				// The allocated bit is in the middle
				// We need to split the range into two
				FreeRange tail = new FreeRange(end, found.end);
				found.end = start;
				// TODO: insert into correct place
				freeList.add(tail);
			}
		}

		public FreeList() {
		}

		synchronized void release(int assigned, int size) {
			int findStart = assigned + size;
			int findEnd = assigned - size;

			for (FreeRange range : freeList) {
				if (range.start == findStart) {
					range.start = assigned;
					return;
				}
				if (range.end == findEnd) {
					range.end = assigned + size;
					return;
				}
			}

			FreeRange range = new FreeRange(assigned, assigned + size);
			freeList.add(range);
		}

		synchronized int allocate(int size) {
			if (freeList.isEmpty()) {
				return FAIL;
			}

			FreeRange range = freeList.get(0);
			int assigned = range.start;
			range.start += size;
			if (range.start == range.end) {
				freeList.remove(0);
			}

			if (range.start > range.end) {
				throw new IllegalStateException();
			}

			return assigned;
		}

		@Override
		public String toString() {
			return "FreeList [freeList=" + freeList + "]";
		}

	}

	class Zone {
		final int size;
		final FreeList freeList;

		void release(int assigned) {
			freeList.release(assigned, size);
		}

		int allocate() {
			int assigned = freeList.allocate(size);
			if (assigned == FAIL) {
				FreeRange range = replenishZone(this);
				if (range != null) {
					freeList.add(range);
					assigned = freeList.allocate(size);
				}
			}
			return assigned;
		}

		Zone(int size) {
			this.size = size;
			this.freeList = new FreeList();
		}

		@Override
		public String toString() {
			return "Zone [size=" + size + "]";
		}
	}

	final Zone[] zones;
	final FreeList freeList;

	public ZoneAllocator(FreeList freeList) {
		this.freeList = freeList;
		this.zones = buildZones();
	}

	Zone[] buildZones() {
		Zone[] zones = new Zone[64];
		int size = 256;
		for (int i = 0; i < zones.length; i++) {
			zones[i] = new Zone(size);

			size = size + (size / 2);
		}
		return zones;
	}

	FreeRange replenishZone(Zone zone) {
		int zoneSize = zone.size;
		int allocationSize = zoneSize;
		int minSize = (256 * 1024);
		if (zoneSize < minSize) {
			allocationSize = (minSize / zoneSize) * zoneSize;
		}

		// Make sure allocation is a multiple of page_size
		allocationSize = PAGE_SIZE * ((allocationSize + PAGE_SIZE - 1) / PAGE_SIZE);
		
		int allocated = freeList.allocate(allocationSize);
		if (allocated == FAIL)
			return null;

		// Everything is allocated in pages, so everything should end up page aligned
		if ((allocated % PAGE_SIZE) != 0) {
			log.warn("Allocation not page-size aligned");
		}
		
		return new FreeRange(allocated, allocated + allocationSize);
	}

	Zone pickZone(int size) {
		// TODO: Inefficient!! At least use binary search, if not same
		// generating function
		for (Zone zone : zones) {
			if (zone.size >= size)
				return zone;
		}
		return null;
	}

	public int allocate(int size) {
		Zone zone = pickZone(size);
		if (zone == null)
			return FAIL;
		return zone.allocate();
	}

	public static ZoneAllocator buildFreeList(Iterable<CacheFileEntry> entries,
			int start, int limit) {
		final FreeList freeList = new FreeList();

		FreeRange range = new FreeRange(start, limit);
		freeList.add(range);

		for (CacheFileEntry entry : entries) {
			int entryStart = entry.position;
			int entryEnd = entryStart + entry.length;

			freeList.remove(entryStart, entryEnd);
		}

		return new ZoneAllocator(freeList);
	}
}
