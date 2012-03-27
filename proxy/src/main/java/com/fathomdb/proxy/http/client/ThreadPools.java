package com.fathomdb.proxy.http.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPools {

	public static final ExecutorService BOSS_POOL = Executors.newCachedThreadPool();
	public static final ExecutorService WORKER_POOL = Executors.newCachedThreadPool();

}
