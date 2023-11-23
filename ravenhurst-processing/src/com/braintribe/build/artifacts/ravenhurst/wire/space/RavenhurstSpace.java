package com.braintribe.build.artifacts.ravenhurst.wire.space;

import java.net.InetAddress;
import java.net.URL;

import javax.sql.DataSource;

import com.braintribe.build.artifacts.ravenhurst.wire.contract.RavenhurstContract;
import com.braintribe.build.ravenhurst.scanner.ArchivaScanner;
import com.braintribe.build.ravenhurst.scanner.ArtifactoryScanner;
import com.braintribe.build.ravenhurst.scanner.Scanner;
import com.braintribe.logging.Logger;
import com.braintribe.logging.LoggerInitializer;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;
import com.zaxxer.hikari.HikariDataSource;

@Managed
public class RavenhurstSpace implements RavenhurstContract {

	private static Logger logger = Logger.getLogger(RavenhurstSpace.class);

	private enum RepositoryType {
		archiva,
		artifactory
	}

	private static RepositoryType repositoryType;

	private static String dbDriver;
	private static String dbUrl;
	private static String dbUser;
	private static String dbPass;

	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		RavenhurstContract.super.onLoaded(configuration);

		loggerInitializer();
		determineRepositoryType();
		determineDbCredentials();
	}

	public static void determineDbCredentials() {

		if (dbDriver != null) {
			return;
		}

		dbDriver = getEnv("RAVENHURST_REPOSITORY_DB_DRIVER", "com.mysql.jdbc.Driver");

		switch (determineRepositoryType()) {
			case archiva:
				dbUrl = getEnv("RAVENHURST_REPOSITORY_DB_URL", null);
				dbUser = getEnv("RAVENHURST_REPOSITORY_DB_USER", null);
				dbPass = getEnv("RAVENHURST_REPOSITORY_DB_PASS", null);
				break;
			case artifactory:
				dbUrl = getEnv("RAVENHURST_REPOSITORY_DB_URL", null);
				dbUser = getEnv("RAVENHURST_REPOSITORY_DB_USER", null);
				dbPass = getEnv("RAVENHURST_REPOSITORY_DB_PASS", null);
				break;
			default:
				throw new RuntimeException("Unknown environment. Cannot decide which data source credentials to provide.");
		}

		if (StringTools.isAnyBlank(dbUrl, dbUser, dbPass)) {
			throw new RuntimeException(
					"Please provide the following environment variables: RAVENHURST_REPOSITORY_DB_URL, RAVENHURST_REPOSITORY_DB_USER, RAVENHURST_REPOSITORY_DB_PASS");
		}

		logger.info("Determined DB connection: URL: " + dbUrl + ", user: " + dbUser);
	}

	private static String getEnv(String key, String defaultValue) {
		String value = System.getProperty(key);
		if (value == null) {
			value = System.getenv(key);
		}
		if (value == null || value.trim().length() == 0) {
			return defaultValue;
		}
		return value;
	}

	private static RepositoryType determineRepositoryType() {

		if (repositoryType != null) {
			return repositoryType;
		}

		String envType = getEnv("RAVENHURST_REPOSITORY_TYPE", null);
		if (envType != null) {
			RepositoryType candidate = RepositoryType.valueOf(envType.toLowerCase());
			if (candidate != null) {
				repositoryType = candidate;
				return repositoryType;
			} else {
				logger.info("Unkown repository type: " + envType);
			}
		}

		try {
			InetAddress localHost = InetAddress.getLocalHost();
			String hostName = localHost.getHostName();
			if (hostName != null) {
				hostName = hostName.toLowerCase();

				if (hostName.contains("inf-web")) {
					repositoryType = RepositoryType.archiva;
					return repositoryType;
				} else if (hostName.contains("ip-")) {
					repositoryType = RepositoryType.artifactory;
					return repositoryType;
				} else {
					logger.info("Could not use the hostname " + hostName + " to determine the environment.");
				}
			}
		} catch (Exception e) {
			logger.info("Could not determine local host identity.", e);
		}

		throw new RuntimeException("Please specify the type of the repository by creating environment vairable: RAVENHURST_REPOSITORY_TYPE");
	}

	@Managed
	private LoggerInitializer loggerInitializer() {
		LoggerInitializer bean = new LoggerInitializer();
		try {
			bean.setLoggerConfigUrl(new URL("WEB-INF/logger.properties"));
		} catch (Exception e) {
			logger.debug("Could not initialize logging.", e);
		}
		return bean;
	}

	@Override
	@Managed
	public DataSource dataSource() {

		determineDbCredentials();

		HikariDataSource bean = new HikariDataSource();
		try {
			bean.setDriverClassName(dbDriver);
		} catch (Exception e) {
			throw new RuntimeException("Could not set driver class.", e);
		}
		bean.setJdbcUrl(dbUrl);
		bean.setUsername(dbUser);
		bean.setPassword(dbPass);
		return bean;
	}

	@Override
	@Managed
	public Scanner scanner() {
		switch (determineRepositoryType()) {
			case archiva:
				return archivaScanner();
			case artifactory:
				return artifactoryScanner();
			default:
				throw new RuntimeException("Unknown environment. Cannot decide which scanner to provide.");
		}
	}

	@Managed
	private ArchivaScanner archivaScanner() {
		ArchivaScanner bean = new ArchivaScanner();
		bean.setDatasource(dataSource());
		bean.setDbTable("ARCHIVA_ARTIFACT");
		return bean;
	}
	@Managed
	private ArtifactoryScanner artifactoryScanner() {
		ArtifactoryScanner bean = new ArtifactoryScanner();
		bean.setDatasource(dataSource());
		bean.setDbTable("nodes");
		bean.setPostgres(getDbDriver().contains("postgres"));
		return bean;
	}

	@Override
	@Managed
	public String dateTimeFormat() {
		return "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	}

	public static String getDbDriver() {
		return dbDriver;
	}
	public static String getDbUrl() {
		return dbUrl;
	}
	public static String getDbUser() {
		return dbUser;
	}

}
