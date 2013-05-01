package com.fathomdb.jpa;

import javax.inject.Inject;
import javax.inject.Provider;

import org.platformlayer.metrics.MetricRegistry;

import com.fathomdb.jdbc.JdbcConnection;
import com.fathomdb.jpa.impl.ResultSetMappers;

public class QueryFactory {
	@Inject
	Provider<JdbcConnection> connectionProvider;

	@Inject
	Provider<ResultSetMappers> resultSetMappersProvider;

	@Inject
	MetricRegistry metricsSystem;

	public <T> T get(Class<T> interfaceType) {
		JdbcClassProxy<T> proxy = JdbcClassProxy.get(metricsSystem, interfaceType);

		JdbcConnection connection = connectionProvider.get();

		return proxy.buildHandler(resultSetMappersProvider, connection);
	}
}
