// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.ravenhurst.scanner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;

import javax.sql.DataSource;

import com.braintribe.logging.Logger;

public class ArchivaScanner implements Scanner {

	private static Logger logger = Logger.getLogger(ArchivaScanner.class);

	private DataSource datasource;
	private String dbTable;

	public void setDbTable(String dbTable) {
		this.dbTable = dbTable;
	}
	public void setDatasource(DataSource datasource) {
		this.datasource = datasource;
	}

	@Override
	public ChangedArtifacts getChangedArtifacts(String repository, Date timestamp) throws ScannerException {

		Connection dbConnection = null;
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		Collection<String> result = new LinkedHashSet<>();
		Date lastUpdate = null;

		try {

			//
			// retrieve connection from data source
			//
			try {
				dbConnection = datasource.getConnection();
			} catch (SQLException e) {
				String msg = "retrieve connection from the datasource as " + e;
				logger.error(msg, e);
				throw new ScannerException(msg, e);
			}
			//
			// prepare statement
			//

			try {
				if (timestamp != null) {
					// time stamp passed - get everything after
					pstmt = dbConnection.prepareStatement("Select GROUP_ID,ARTIFACT_ID,VERSION,FILE_TYPE,LAST_MODIFIED FROM " + dbTable
							+ " WHERE LAST_MODIFIED > ? ORDER BY GROUP_ID ASC, ARTIFACT_ID ASC, VERSION ASC");
					Timestamp sqlTimestamp = new Timestamp(timestamp.getTime());
					pstmt.setTimestamp(1, sqlTimestamp);
				} else {
					// no time stamp passed - get everything
					pstmt = dbConnection.prepareStatement("Select GROUP_ID,ARTIFACT_ID,VERSION,FILE_TYPE,LAST_MODIFIED FROM " + dbTable
							+ " ORDER BY GROUP_ID ASC, ARTIFACT_ID ASC, VERSION ASC");
				}

			} catch (SQLException e) {
				String msg = "cannot prepare statment as " + e;
				logger.error(msg, e);
				throw new ScannerException(msg, e);
			}

			//
			// execute query and build return value
			//

			try {
				resultSet = pstmt.executeQuery();
				while (resultSet.next()) {
					String groupId = resultSet.getString("GROUP_ID");
					String artifactId = resultSet.getString("ARTIFACT_ID");
					String version = resultSet.getString("VERSION");
					Date date = resultSet.getDate("LAST_MODIFIED");

					if (lastUpdate == null || lastUpdate.compareTo(date) < 0) {
						lastUpdate = date;
					}

					// String partType = resultSet.getString( "FILE_TYPE");

					String artifact = groupId + ":" + artifactId + "#" + version;
					result.add(artifact);
				}
			} catch (SQLException e) {
				String msg = "cannot execute statment as " + e;
				logger.error(msg, e);
				throw new ScannerException(msg, e);
			}
		}

		// clean up
		finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Throwable e) {
				String msg = "cannot close result set as " + e;
				logger.error(msg, e);
			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Throwable e) {
				String msg = "cannot close prepared statement as " + e;
				logger.error(msg, e);

			}
			try {
				if (dbConnection != null) {
					dbConnection.close();
				}
			} catch (Throwable e) {
				String msg = "cannot connection as " + e;
				logger.error(msg, e);
			}
		}

		return new ChangedArtifacts(result, lastUpdate);
	}

	@Override
	public Long getArtifactTimeStamp(String repository, String group, String artifact, String version) throws ScannerException {
		Long result = null;

		Connection dbConnection = null;
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {

			//
			// retrieve connection from data source
			//
			try {
				dbConnection = datasource.getConnection();
			} catch (SQLException e) {
				String msg = "retrieve connection from the datasource as " + e;
				logger.error(msg, e);
				throw new ScannerException(msg, e);
			}
			//
			// prepare statement
			//

			try {

				//
				pstmt = dbConnection.prepareStatement(
						"Select LAST_MODIFIED FROM " + dbTable + " WHERE GROUP_ID = ? AND ARTIFACT_ID = ? AND VERSION = ? ORDER BY LAST_MODIFIED");

				pstmt.setString(1, group);
				pstmt.setString(2, artifact);
				pstmt.setString(3, version);

			} catch (SQLException e) {
				String msg = "cannot prepare statement as " + e;
				logger.error(msg, e);
				throw new ScannerException(msg, e);
			}

			//
			// execute query and build return value
			//
			try {
				resultSet = pstmt.executeQuery();
				while (resultSet.next()) {
					Timestamp timestamp = resultSet.getTimestamp("LAST_MODIFIED");
					return timestamp.getTime();
				}
			} catch (SQLException e) {
				String msg = "cannot execute statment as " + e;
				logger.error(msg, e);
				throw new ScannerException(msg, e);
			}
		}

		// clean up
		finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Throwable e) {
				String msg = "cannot close result set as " + e;
				logger.error(msg, e);
			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Throwable e) {
				String msg = "cannot close prepared statement as " + e;
				logger.error(msg, e);

			}
			try {
				if (dbConnection != null) {
					dbConnection.close();
				}
			} catch (Throwable e) {
				String msg = "cannot connection as " + e;
				logger.error(msg, e);
			}
		}

		return result;
	}

	@Override
	public String toString() {
		return "Archiva Scanner";
	}
}
