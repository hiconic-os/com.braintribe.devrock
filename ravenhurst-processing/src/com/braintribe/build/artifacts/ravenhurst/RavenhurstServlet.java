// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifacts.ravenhurst;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.braintribe.build.artifacts.ravenhurst.wire.contract.RavenhurstContract;
import com.braintribe.build.artifacts.ravenhurst.wire.space.RavenhurstSpace;
import com.braintribe.build.ravenhurst.scanner.ChangedArtifacts;
import com.braintribe.build.ravenhurst.scanner.Scanner;
import com.braintribe.build.ravenhurst.scanner.ScannerException;
import com.braintribe.logging.Logger;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;
import com.braintribe.utils.html.HtmlTools;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 *
 * Ravenhurst: servlet to answer Malaclypse's questions about changes in Archiva's database
 *
 * accesses Archiva's DB, runs a query and returns a simple string with a newline delimited string with condensed artifact names of all artifacts that
 * were changed after the passed timestamp. If no timestamp's given, it returns a list of all artifacts in the db.
 *
 *
 * @author pit
 */
public class RavenhurstServlet extends HttpServlet {

	private static Logger logger = Logger.getLogger(RavenhurstServlet.class);
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	protected WireContext<RavenhurstContract> context;

	private String parameterTimestamp = "timestamp";
	private String parameterGroup = "groupid";
	private String parameterArtifact = "artifactid";
	private String parameterVersion = "Version";

	private Scanner scanner;
	private String format;

	private enum Action {
		changes,
		timestamp,
		_about
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			handleRequest(request, response);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String msg = "cannot process request " + sw.toString();
			logger.error(msg, e);
			throw new ServletException(msg, e);
		}
	}

	private void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		if (request == null) {
			return;
		}

		Action action = null;
		String repository = null;

		String path = request.getPathInfo();
		if (path != null) {

			String[] pathParts = path.split("\\/");

			for (String part : pathParts) {

				if (part == null || part.trim().length() == 0 || part.trim().equals("/")) {
					continue;
				}

				Action actionCandidate = null;
				for (Action candidate : Action.values()) {
					if (candidate.name().equals(part)) {
						actionCandidate = candidate;
						break;
					}
				}
				if (actionCandidate != null) {
					action = actionCandidate;
				} else {
					repository = part;
				}

			}

		}

		if (action == null) {
			logger.error("Could not determine action from path " + path);
			action = Action.changes;
		}
		logger.debug("Action: " + action + ", Repository: " + repository);

		// add new paths here
		switch (action) {
			case changes:
				handleChangesRequest(request, response, repository);
				break;
			case timestamp:
				handleTimestampRequest(request, response, repository);
				break;
			case _about:
				handleAboutRequest(response);
				break;
		}

	}

	private void handleAboutRequest(HttpServletResponse response) throws ServletException {

		Map<String, String> map = new LinkedHashMap<>();
		collectJvmInfo(map);
		collectDbInfo(map);
		collectScannerInfo(map);

		response.setContentType("text/html");
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			throw new ServletException("Could not get response writer", e);
		}

		StringBuilder sb = new StringBuilder();
		sb.append("<html><body><h1>Ravenhurst 2.0</h1><br />\n");
		sb.append("<table>\n");
		for (Map.Entry<String, String> entry : map.entrySet()) {
			sb.append(" <tr><td>\n");
			sb.append(HtmlTools.escapeHtml(entry.getKey()));
			sb.append(":\n </td><td>\n");
			sb.append(HtmlTools.escapeHtml(entry.getValue()));
			sb.append("\n </td></tr>\n");
		}
		sb.append("</table></body></html>\n");
		writer.println(sb.toString());

	}

	private void collectDbInfo(Map<String, String> map) {
		RavenhurstSpace.determineDbCredentials();
		map.put("DB Driver", RavenhurstSpace.getDbDriver());
		map.put("DB URL", RavenhurstSpace.getDbUrl());
		map.put("DB User", RavenhurstSpace.getDbUser());

		if (context == null || context.contract() == null || context.contract().dataSource() == null) {
			map.put("Database", "None available");
		} else {
			try {
				DataSource dataSource = context.contract().dataSource();

				Instant start = NanoClock.INSTANCE.instant();
				try (Connection c = dataSource.getConnection()) {
					boolean valid = c.isValid(5);
					map.put("DB Connection valid", "" + valid);
				} catch (Exception e) {
					map.put("DB Connection", "Error: " + e.getMessage());
					logger.error("Error while trying to test DB connection.", e);
				}
				Instant stop = NanoClock.INSTANCE.instant();
				map.put("DB Connection test", StringTools.prettyPrintDuration(Duration.between(start, stop), true, null));
			} catch (Exception e) {
				map.put("DB Data Source", "Error: " + e.getMessage());
				logger.error("Error while trying to access DB data source.", e);
			}
		}
	}

	private void collectScannerInfo(Map<String, String> map) {
		if (scanner == null) {
			map.put("Scanner", "None available");
		} else {
			map.put("Scanner", scanner.toString());
		}
	}

	private static void collectJvmInfo(Map<String, String> map) {
		try {
			RuntimeMXBean rmx = ManagementFactory.getRuntimeMXBean();
			Properties sysProps = System.getProperties();
			map.put("VM Name", rmx.getVmName());
			map.put("VM Vendor", rmx.getVmVendor());
			map.put("VM Version", rmx.getVmVersion());
			map.put("Java Version", sysProps.getProperty("java.version"));
			map.put("Java Vendor", sysProps.getProperty("java.vendor"));
		} catch (Exception e) {
			logger.error("Error while trying to collect Java information", e);
		}
	}

	private void handleChangesRequest(HttpServletRequest request, HttpServletResponse response, String repository) throws ServletException {

		String param = request.getParameter(parameterTimestamp);
		try {
			ChangedArtifacts changedArtifacts = null;
			if (param == null) {
				changedArtifacts = scanner.getChangedArtifacts(repository, null);
			} else {
				SimpleDateFormat sdf = new SimpleDateFormat(format);
				Date date = sdf.parse(param);
				changedArtifacts = scanner.getChangedArtifacts(repository, date);
			}
			String charset = "ISO-8859-1";
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			MessageDigest digest = MessageDigest.getInstance("MD5");

			try (Writer writer = new OutputStreamWriter(new DigestOutputStream(baos, digest), charset)) {
				boolean first = true;
				for (String artifact : changedArtifacts.getArtifacts()) {
					if (first) {
						first = false;
					} else {
						writer.append("\n");
					}
					writer.append(artifact);
				}
			}

			Date lastUpdate = changedArtifacts.getLastUpdate();
			if (lastUpdate != null) {
				response.setDateHeader("Last-Update", lastUpdate.getTime());
			}

			String md5 = StringTools.toHex(digest.digest());

			response.setContentType("text/plain");
			response.setCharacterEncoding(charset);
			response.setHeader("X-Checksum-Md5", md5);

			baos.writeTo(response.getOutputStream());

		} catch (ParseException e) {
			String msg = "cannot parse date as " + e;
			logger.error(msg, e);
			throw new ServletException(msg, e);
		} catch (ScannerException e) {
			String msg = "cannot scan for changed artifacts as " + e;
			logger.error(msg, e);
			throw new ServletException(msg, e);
		} catch (IOException e) {
			String msg = "cannot print return string as " + e;
			logger.error(msg, e);
			throw new ServletException(msg, e);
		} catch (NoSuchAlgorithmException e) {
			String msg = "cannot find message digest for MD5 as " + e;
			logger.error(msg, e);
			throw new ServletException(msg, e);
		}
	}

	private void handleTimestampRequest(HttpServletRequest request, HttpServletResponse response, String repository) throws ServletException {

		try {
			String groupId = request.getParameter(parameterGroup);
			String artifactId = request.getParameter(parameterArtifact);
			String version = request.getParameter(parameterVersion);
			response.setContentType("text/plain");

			Long result = scanner.getArtifactTimeStamp(repository, groupId, artifactId, version);
			if (result != null) {
				Date date = new Date(result);
				SimpleDateFormat sdf = new SimpleDateFormat(format);
				String value = sdf.format(date);
				response.getWriter().print(value);
			} else {
				response.getWriter().print("not present");
			}

		} catch (ScannerException e) {
			String msg = "cannot print return string as " + e;
			logger.error(msg, e);
			throw new ServletException(msg, e);
		} catch (IOException e) {
			String msg = "cannot print return string as " + e;
			logger.error(msg, e);
			throw new ServletException(msg, e);
		}

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	public void destroy() {
		try {
			context.shutdown();
		} catch (Exception e) {
			logger.error("Error while trying to shut down Wire context.", e);
		}
		super.destroy();
	}

	@Override
	public void init() throws ServletException {
		super.init();

		context = Wire.context(RavenhurstContract.class).bindContracts("com.braintribe.build.artifacts.ravenhurst.wire").build();

		RavenhurstContract contract = context.contract();

		scanner = contract.scanner();
		format = contract.dateTimeFormat();

	}

}
