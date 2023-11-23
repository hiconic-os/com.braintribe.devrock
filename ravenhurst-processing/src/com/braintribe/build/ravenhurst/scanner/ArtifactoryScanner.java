
// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.ravenhurst.scanner;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.sql.DataSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.braintribe.common.lcd.function.CheckedSupplier;
import com.braintribe.logging.Logger;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.xml.XmlTools;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ArtifactoryScanner implements Scanner {

	private static Logger logger = Logger.getLogger(ArtifactoryScanner.class);

	private DataSource datasource;
	private String dbTable;
	private Map<String, Set<String>> virtualRepositories = new HashMap<>();
	private long lastVirtualRepositoryRead = -1L;
	private long virtualRepositoryReadInterval = 2 * 60 * 1000; // every 2 minutes
	private ReentrantLock virtualRepositoryReadLock = new ReentrantLock();

	private boolean postgres;

	public void setDatasource(DataSource datasource) {
		this.datasource = datasource;
	}

	public void setDbTable(String dbTable) {
		this.dbTable = dbTable;
	}

	public void setPostgres(boolean postgres) {
		this.postgres = postgres;
	}

	private void readVirtualRepositories(boolean force) {
		if (!force && (System.currentTimeMillis() - lastVirtualRepositoryRead) < virtualRepositoryReadInterval) {
			return;
		}
		virtualRepositoryReadLock.lock();
		try {
			if (!force && (System.currentTimeMillis() - lastVirtualRepositoryRead) < virtualRepositoryReadInterval) {
				return;
			}
			lastVirtualRepositoryRead = System.currentTimeMillis();

			logger.debug("Re-reading virtual repository configuration.");

			Map<String, Set<String>> newVirtualRepositories;

			try {
				// since Artifactory 7.49.x repositories are no longer stored in the global configuration,
				// hence we must fetch and parse individual (virtual) repository configurations instead.
				List<String> virtualRepositoryConfigs = fetchVirtualRepositoryConfigsFromDatabase();
				if (virtualRepositoryConfigs != null && !virtualRepositoryConfigs.isEmpty()) {
					newVirtualRepositories = parseVirtualRepositoriesFromVirtualRepositoryConfigs(virtualRepositoryConfigs);
				} else {
					String globalConfig = fetchGlobalConfigFromDatabase();
					if (globalConfig == null) {
						// TODO: should this really just be warning?
						logger.warn("Global configuration is null");
						return;
					}
					newVirtualRepositories = parseVirtualRepositoriesFromGlobalConfig(globalConfig);
				}

				virtualRepositories = newVirtualRepositories;

				if (!newVirtualRepositories.isEmpty()) {
					logger.debug(() -> "Virtual repositories are:\n" + newVirtualRepositories.keySet().stream().sorted() //
							.map(key -> "\t" + key + " -> " + newVirtualRepositories.get(key)) //
							.collect(Collectors.joining("\n")));
				} else {
					// not necessarily an error
					logger.debug("No virtual repositories found.");
				}

			} catch (Exception e) {
				logger.error("Error while rading virtual repositories!", e);
			}

		} finally {
			virtualRepositoryReadLock.unlock();
		}
	}

	private Map<String, Set<String>> parseVirtualRepositoriesFromGlobalConfig(String globalConfig) {
		Map<String, Set<String>> result = new HashMap<>();
		try {
			Document xml = XmlTools.loadXML(new ByteArrayInputStream(globalConfig.getBytes("UTF-8")));

			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath localXpath = xPathfactory.newXPath();

			XPathExpression expr = localXpath.compile("//virtualRepositories/virtualRepository");
			NodeList nl = (NodeList) expr.evaluate(xml, XPathConstants.NODESET);
			int len = nl.getLength();
			for (int i = 0; i < len; ++i) {
				Node item = nl.item(i);
				NodeList childNodes = item.getChildNodes();

				String virtualRepositoryName = null;
				Set<String> references = new LinkedHashSet<>();

				int cLen = childNodes.getLength();
				for (int j = 0; j < cLen; ++j) {

					Node child = childNodes.item(j);
					String nodeName = child.getNodeName();
					switch (nodeName) {
						case "key":
							virtualRepositoryName = child.getTextContent();
							break;
						case "repositories":

							NodeList refNodes = child.getChildNodes();
							int rLen = refNodes.getLength();
							for (int k = 0; k < rLen; ++k) {
								Node refNode = refNodes.item(k);
								String ref = refNode.getTextContent();
								if (ref != null) {
									ref = ref.trim();
									if (!StringTools.isEmpty(ref)) {
										references.add(ref);
									}
								}
							}

							break;
						default:
							break;
					}
				}
				if (virtualRepositoryName != null) {
					result.put(virtualRepositoryName, references);
				}
			}

		} catch (Exception e) {
			throw new IllegalStateException("Error while parsing virtual repositories from global config:\n" + globalConfig, e);
		}
		return result;
	}

	private Map<String, Set<String>> parseVirtualRepositoriesFromVirtualRepositoryConfigs(List<String> virtualRepositoryConfigs) {

		// a virtual repo config is a json, which looks like this:
		// @formatter:off
//{
//"type":"virtual",
//"key":"example-virtual-repository",
//"packageType":"maven",
//"baseConfig":{
//  "modelVersion":0,
//  "description":"",
//  "notes":"",
//  "repoLayoutRef":"maven-2-default",
//  "includesPattern":"**/*",
//  "excludesPattern":""
//},
//"repoTypeConfig":{
//  "artifactoryRequestsCanRetrieveRemoteArtifacts":false,
//  "virtualCacheConfig":{
//     "virtualRetrievalCachePeriodSecs":600
//  },
//  "repositoryRefs":[
//     "example-target-repository-1",
//       "example-target-repository-2"
//  ]
//},
//"packageTypeConfig":{
//  "pomRepositoryReferencesCleanupPolicy":"discard_active_reference",
//  "forceMavenAuthentication":"false"
//},
//"securityConfig":{
//  "hideUnauthorizedResources":false,
//  "signedUrlTtl":90
//},
//"repoType":"VIRTUAL"
//}
		// @formatter:on

		Map<String, Set<String>> result = new HashMap<>();

		ObjectMapper mapper = new ObjectMapper();

		for (String virtualRepositoryConfig : virtualRepositoryConfigs) {

			try {
				Map<String, Object> virtualRepositoryConfigAsMap = mapper.readValue(virtualRepositoryConfig, Map.class);
				String virtualRepositoryKey = (String) virtualRepositoryConfigAsMap.get("key");
				@SuppressWarnings("unchecked")
				List<String> repositoryRefs = (List<String>) ((Map<String, Object>) virtualRepositoryConfigAsMap.get("repoTypeConfig"))
						.get("repositoryRefs");

				result.put(virtualRepositoryKey, new HashSet<>(repositoryRefs));

			} catch (JsonMappingException e) {
				throw new IllegalStateException("Error while reading virtual repository configuration json:\n" + virtualRepositoryConfig, e);
			} catch (JsonProcessingException e) {
				throw new IllegalStateException("Error while reading virtual repository configuration json:\n" + virtualRepositoryConfig, e);
			}
		}

		return result;
	}

	private String fetchGlobalConfigFromDatabase() {
		ResultSet rs = null;
		Connection c = null;
		Statement st = null;
		try {
			c = datasource.getConnection();
			st = c.createStatement();
			rs = st.executeQuery("select data from configs where config_name = 'artifactory.config.xml'");
			if (rs.next()) {
				final CheckedSupplier<InputStream, Exception> isp;

				if (postgres) {
					byte[] bytes = rs.getBytes(1);
					isp = () -> new ByteArrayInputStream(bytes);
				} else {
					Blob blob = rs.getBlob(1);
					isp = () -> blob.getBinaryStream();
				}

				String config = null;
				try (InputStream in = isp.get()) {
					config = IOTools.slurp(in, "UTF-8");
				}
				return config;

			} else {
				logger.warn("Could not find artifactory.config.xml in the database.");
			}

		} catch (Exception e) {
			logger.error("Error while trying to read virtual repository configuration from database", e);
		} finally {
			IOTools.closeCloseable(rs, logger);
			IOTools.closeCloseable(st, logger);
			IOTools.closeCloseable(c, logger);
		}
		return null;
	}

	private List<String> fetchVirtualRepositoryConfigsFromDatabase() {
		List<String> result = new ArrayList<>();

		ResultSet rs = null;
		Connection c = null;
		Statement st = null;
		try {
			c = datasource.getConnection();
			st = c.createStatement();
			rs = st.executeQuery("select repository_key, config_blob from repository_config where type = 'virtual'");

			while (rs.next()) {
				final CheckedSupplier<InputStream, Exception> isp;

				if (postgres) {
					byte[] bytes = rs.getBytes(2);
					isp = () -> new ByteArrayInputStream(bytes);
				} else {
					Blob blob = rs.getBlob(2);
					isp = () -> blob.getBinaryStream();
				}

				String config = null;
				try (InputStream in = isp.get()) {
					config = IOTools.slurp(in, "UTF-8");
				}
				result.add(config);
			}
		} catch (Exception e) {
			logger.error("Error while trying to read virtual repository configurations from database", e);
			result = null; // return null to be consistent with readConfigFromDatabase()
		} finally {
			IOTools.closeCloseable(rs, logger);
			IOTools.closeCloseable(st, logger);
			IOTools.closeCloseable(c, logger);
		}
		return result;
	}

	private Set<String> resolveRepository(String repo) {
		if (!virtualRepositories.containsKey(repo)) {
			return asSet(repo);
		}
		return resolveRepository(repo, new HashSet<String>());
	}

	private Set<String> resolveRepository(String repo, Set<String> visitedRepositories) {
		visitedRepositories.add(repo);

		Set<String> refs = virtualRepositories.get(repo);
		Set<String> result = new HashSet<>();
		result.add(repo);
		if (refs != null) {
			result.addAll(refs);
			for (String ref : refs) {
				if (!visitedRepositories.contains(ref)) {
					Set<String> nestedSet = resolveRepository(ref, visitedRepositories);
					if (nestedSet != null) {
						result.addAll(nestedSet);
					}
				}
			}
		}
		return result;
	}

	@Override
	public ChangedArtifacts getChangedArtifacts(String repository, Date timestamp) throws ScannerException {

		readVirtualRepositories(false);
		Set<String> repositories = resolveRepository(repository);
		int repoCount = repositories.size();

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

				StringBuilder sb = new StringBuilder(
						"SELECT node_path, MAX(modified) FROM " + dbTable + " WHERE node_type = 1 AND repo IN (");
				for (int i = 0; i < repoCount; ++i) {
					if (i > 0) {
						sb.append(',');
					}
					sb.append('?');
				}
				sb.append(") AND node_name != 'maven-metadata.xml'");

				if (timestamp != null) {
					// time stamp passed - get everything after
					sb.append(" AND modified > ?");
				} else {
					// no time stamp passed - get everything
				}

				// for each node path order by modified descending and then group by node path to only get last modified date per node path
				sb.append(" GROUP BY node_path");

				pstmt = dbConnection.prepareStatement(sb.toString());
				int index = 1;
				for (String r : repositories) {
					pstmt.setString(index++, r);
				}
				if (timestamp != null) {
					pstmt.setLong(index, timestamp.getTime());
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
					String nodePath = resultSet.getString(1);
					Date date = new Date(resultSet.getLong(2));

					if (lastUpdate == null || lastUpdate.compareTo(date) < 0) {
						lastUpdate = date;
					}

					String artifact = parseNodePath(nodePath);
					if (artifact != null) {
						result.add(artifact);
					}
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

	private static String parseNodePath(String nodePath) {
		if (nodePath == null) {
			return null;
		}
		try {
			nodePath = nodePath.trim();
			if (nodePath.endsWith("/")) {
				nodePath = nodePath.substring(0, nodePath.length() - 1);
			}
			String[] parts = nodePath.split("\\/");
			if (parts.length <= 1) {
				return null;
			}
			int i = 0;
			StringBuilder groupId = new StringBuilder();
			for (; i < parts.length - 2; ++i) {
				if (groupId.length() > 0) {
					groupId.append('.');
				}
				groupId.append(parts[i]);
			}
			String artifactId = parts[i];
			String version = parts[i + 1];
			if (!containsDigit(version)) {
				return null;
			}
			return groupId.toString() + ":" + artifactId + "#" + version;
		} catch (Exception e) {
			throw new RuntimeException("Error while processing nodePath: " + nodePath, e);
		}
	}
	private static boolean containsDigit(final String s) {
		if (s != null && !s.isEmpty()) {
			for (char c : s.toCharArray()) {
				if (Character.isDigit(c)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public Long getArtifactTimeStamp(String repository, String group, String artifact, String version) throws ScannerException {

		readVirtualRepositories(false);
		Set<String> repositories = resolveRepository(repository);
		int repoCount = repositories.size();

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

				group = group.replace('.', '/');
				String nodePath = group + "/" + artifact + "/" + version;

				StringBuilder sb = new StringBuilder("select modified FROM " + dbTable + " WHERE node_type = 1 and repo in (");
				for (int i = 0; i < repoCount; ++i) {
					if (i > 0) {
						sb.append(',');
					}
					sb.append('?');
				}
				sb.append(") and node_path = ? ORDER BY modified");

				pstmt = dbConnection.prepareStatement(sb.toString());
				int index = 1;
				for (String r : repositories) {
					pstmt.setString(index++, r);
				}
				pstmt.setString(index, nodePath);

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
					Long timestamp = resultSet.getLong(1);
					return timestamp;
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

		readVirtualRepositories(true);

		StringBuilder sb = new StringBuilder("Artifactory Scanner\n");
		for (Map.Entry<String, Set<String>> entry : virtualRepositories.entrySet()) {
			sb.append("\nVirtual repository: ");
			sb.append(entry.getKey());
			for (String ref : entry.getValue()) {
				sb.append("\n  references: ");
				sb.append(ref);
			}
		}
		return sb.toString();
	}
}
