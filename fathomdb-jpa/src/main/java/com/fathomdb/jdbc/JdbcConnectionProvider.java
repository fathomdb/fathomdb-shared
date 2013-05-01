package com.fathomdb.jdbc;

import javax.inject.Provider;

class JdbcConnectionProvider implements Provider<JdbcConnection> {

	@Override
	public JdbcConnection get() {
		return JdbcTransactionInterceptor.getConnection();
	}

}
