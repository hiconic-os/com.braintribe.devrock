package com.braintribe.build.artifacts.ravenhurst;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Ignore;

import com.braintribe.processing.test.db.derby.DerbyServerControl;
import com.braintribe.utils.FileTools;
import com.zaxxer.hikari.HikariDataSource;

public abstract class AbstractTestBase {

	protected static AbstractTestBase instance;

	public static int DERBY_PORT = 1527;

	protected DerbyServerControl derbyServerControl = null;
	protected HikariDataSource datasource = null;
	protected RavenhurstServlet servlet = null;

	public void initializeDatabase() throws Exception {
		deleteDatabaseFiles();

		if (!DerbyServerControl.checkDerbyServerPortAvailable(DERBY_PORT)) {
			print("Derby port " + DERBY_PORT + " is not available.");
		}

		derbyServerControl = new DerbyServerControl();
		derbyServerControl.setLogConnections(true);
		// derbyServerControl.setTraceDirectory(new File("log"));
		derbyServerControl.setPort(DERBY_PORT);
		derbyServerControl.setLogWriter(new PrintWriter(System.out, true));
		derbyServerControl.start();

		initializeTables();
		System.setProperty("RAVENHURST_REPOSITORY_DB_DRIVER", "org.apache.derby.jdbc.ClientDriver");
		System.setProperty("RAVENHURST_REPOSITORY_DB_URL",
				"jdbc:derby://localhost:" + AbstractTestBase.DERBY_PORT + "/res/db/ravenhursttests;create=false");
		System.setProperty("RAVENHURST_REPOSITORY_DB_USER", "cortex");
		System.setProperty("RAVENHURST_REPOSITORY_DB_PASS", "cortex");
		initializeServletEnvironment();

		servlet = new RavenhurstServlet();
		servlet.init();
	}

	protected abstract void initializeServletEnvironment();

	protected HikariDataSource getDataSource() {
		if (datasource != null) {
			return datasource;
		}
		datasource = new HikariDataSource();
		try {
			datasource.setDriverClassName("org.apache.derby.jdbc.ClientDriver");
		} catch (Exception e) {
			throw new RuntimeException("Could not set driver class.", e);
		}
		datasource.setJdbcUrl("jdbc:derby://localhost:" + AbstractTestBase.DERBY_PORT + "/res/db/ravenhursttests;create=true");
		datasource.setUsername("cortex");
		datasource.setPassword("cortex");
		return datasource;
	}

	protected abstract void initializeTables() throws Exception;

	public void shutdownDatabase() throws Exception {

		if (datasource != null) {
			try {
				datasource.close();
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}

		if (derbyServerControl != null) {
			derbyServerControl.stop();
		}

		deleteDatabaseFiles();
		File logFile = new File("derby.log");
		if (logFile.exists()) {
			logFile.delete();
		}
	}

	protected static void print(String text) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
		System.out.println(sdf.format(new Date()) + " [Master]: " + text);
	}

	@Ignore
	protected static void deleteDatabaseFiles() throws IOException {
		File dbFolder = new File("res/db");
		if (dbFolder.exists() && dbFolder.isDirectory()) {
			FileTools.deleteDirectoryRecursively(dbFolder);
		}
	}

}
