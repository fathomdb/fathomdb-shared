package com.fathomdb.interceptors.retry;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

public class RetryOnErrorModule extends AbstractModule {
	@Override
	protected void configure() {
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(RetryOnError.class), new RetryOnErrorMethodInterceptor());
	}
}