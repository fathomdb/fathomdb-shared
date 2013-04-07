package com.fathomdb.interceptors.retry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks that the specified method should automatically catch Exceptions, and be automatically re-tried in case of error
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Inherited
public @interface RetryOnError {
	Class<? extends Throwable>[] value();

	int maxAttempts() default 3;
}
