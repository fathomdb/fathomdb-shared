package com.fathomdb.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.postgresql.PGStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.jpa.impl.ConnectionMetadata;
import com.google.common.collect.Maps;

public class JdbcConnection {
	private static final Logger log = LoggerFactory.getLogger(JdbcConnection.class);

	final ConnectionMetadata metadata;
	final Connection connection;

	final Map<String, PreparedStatement> batches = Maps.newHashMap();

	public JdbcConnection(ConnectionMetadata metadata, Connection connection) {
		super();
		this.metadata = metadata;
		this.connection = connection;
	}

	private Connection getConnection() {
		return connection;
	}

	public ConnectionMetadata getConnectionMetadata() {
		return metadata;
	}

	public <K, V> V getCacheable(final K key, final Callable<V> fn) {
		return metadata.getCacheable(key, fn);
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		PreparedStatement preparedStatement = getConnection().prepareStatement(sql);

		ensurePrepared(preparedStatement);

		return preparedStatement;
	}

	static int warnCount = 0;

	private void ensurePrepared(PreparedStatement preparedStatement) throws SQLException {
		PreparedStatement actual = preparedStatement;

		try {
			if (actual.getClass().getName().equals("com.jolbox.bonecp.PreparedStatementHandle")) {
				actual = actual.unwrap(PreparedStatement.class);

				// PreparedStatementHandle handle = (PreparedStatementHandle) actual;
				// actual = handle.getInternalPreparedStatement();
			}

			if (actual.isWrapperFor(PGStatement.class)) {
				PGStatement pgStatement = actual.unwrap(PGStatement.class);
				// Make sure that we actually prepare the statement
				pgStatement.setPrepareThreshold(1);
			}
		} catch (SQLFeatureNotSupportedException e) {
			if (warnCount < 10) {
				log.info("Unable to force prepare of statement - not supported");
				warnCount++;
			}
		} catch (Exception e) {
			if (warnCount < 20) {
				warnCount++;
				log.info("Unable to force prepare of statement {}", e.getMessage());
			}
		}

		// if (actual instanceof AbstractJdbc2Statement) {
		// // Make sure that we actually prepare the statement
		// ((AbstractJdbc2Statement) actual).setPrepareThreshold(1);
		// }
	}

	public PreparedStatement prepareBatchStatement(String sql) throws SQLException {
		PreparedStatement ps = batches.get(sql);
		if (ps == null) {
			ps = prepareStatement(sql);
			batches.put(sql, ps);
		}

		return ps;
	}

	public void commit() throws SQLException {
		if (!batches.isEmpty()) {
			for (Entry<String, PreparedStatement> entry : batches.entrySet()) {
				String sql = entry.getKey();
				PreparedStatement preparedStatement = entry.getValue();
				log.debug("Flushing batch operation: " + sql);

				int[] rowCounts = preparedStatement.executeBatch();
				for (int rowCount : rowCounts) {
					if (rowCount < 0) {
						throw new SQLException("Error flushing batch operation: " + sql);
					}

					if (rowCount != 1) {
						throw new SQLException("Unexpected row count from batch operation: " + sql);
					}
				}

				preparedStatement.close();
			}
			batches.clear();
		}
		connection.commit();
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		PreparedStatement ps = connection.prepareStatement(sql, columnNames);

		ensurePrepared(ps);

		return ps;
	}

}
