package com.fathomdb.interceptors.retry;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryOnErrorMethodInterceptor implements MethodInterceptor {
	static final Logger log = LoggerFactory.getLogger(RetryOnErrorMethodInterceptor.class);

	public RetryOnErrorMethodInterceptor() {
	}

	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		Object result;

		Method method = methodInvocation.getMethod();
		RetryOnError annotation = method.getAnnotation(RetryOnError.class);
		if (annotation == null) {
			throw new IllegalStateException();
		}

		for (int attempt = 1; attempt <= annotation.maxAttempts(); attempt++) {
			try {
				result = methodInvocation.proceed();
				return result;
			} catch (Throwable e) {
				if (attempt == annotation.maxAttempts()) {
					throw e;
				}

				Class<?> exceptionClass = e.getClass();
				Class<?> match = null;
				for (Class<?> candidate : annotation.value()) {
					if (candidate.isAssignableFrom(exceptionClass)) {
						match = candidate;
						break;
					}
				}

				if (match != null) {
					log.info("Retrying after Exception", e);
				} else {
					throw e;
				}
			}
		}

		throw new IllegalStateException();
	}
}
