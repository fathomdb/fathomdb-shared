package com.fathomdb.ratelimit;

public class Clock {
	/**
	 * Returns the current monotonically increasing time, in seconds This may not be exact; but should be close enough
	 * for object expiration.
	 * 
	 * Currently we call System.nanoTime, but we may switch to a timer-updated value in future
	 */
	public static int getMonotime() {
		long nanoTime = System.nanoTime();
		nanoTime /= 1000000000L;

		return (int) nanoTime;
	}

	public static int getUnixTime() {
		long time = System.currentTimeMillis();
		time /= 1000L;
		return (int) time;
	}
}
