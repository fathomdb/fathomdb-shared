package com.fathomdb.proxy.http.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ThreadPools {

	public static final ExecutorService BOSS_POOL = Executors
			.newCachedThreadPool();
	public static final ExecutorService WORKER_POOL = Executors
			.newCachedThreadPool();

	public static final ScheduledExecutorService SYSTEM_TASK_POOL = Executors
			.newScheduledThreadPool(2);

}
