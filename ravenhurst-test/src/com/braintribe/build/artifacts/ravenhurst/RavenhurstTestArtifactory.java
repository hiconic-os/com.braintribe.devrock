package com.braintribe.build.artifacts.ravenhurst;

import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.build.artifacts.ravenhurst.test.TestHttpServletRequest;
import com.braintribe.build.artifacts.ravenhurst.test.TestHttpServletResponse;
import com.braintribe.build.artifacts.ravenhurst.wire.contract.RavenhurstContract;
import com.braintribe.build.ravenhurst.scanner.ArtifactoryScanner;
import com.braintribe.build.ravenhurst.scanner.Scanner;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;
import com.zaxxer.hikari.HikariDataSource;

public class RavenhurstTestArtifactory extends AbstractTestBase {

	@BeforeClass
	public static void beforeClass() throws Exception {
		instance = new RavenhurstTestArtifactory();
		instance.initializeDatabase();
	}

	@AfterClass
	public static void afterClass() throws Exception {
		if (instance != null) {
			instance.shutdownDatabase();
		}
	}

	@Override
	protected void initializeTables() throws Exception {

		HikariDataSource datasource = instance.getDataSource();
		List<String> lines = FileTools.readLines(new File("res/artifactory-data.txt"), "UTF-8");
		Connection c = null;
		Statement st = null;
		PreparedStatement ps = null;
		InputStream in = null;
		try {

			c = datasource.getConnection();
			st = c.createStatement();

			st.executeUpdate(
					"create table nodes (node_id bigint, node_type int, repo varchar(64), node_path varchar(1024), node_name varchar(255), depth int, created bigint, created_by varchar(64), modified bigint, modified_by varchar(64), updated bigint, bin_length bigint, sha1_actual varchar(40), sha1_original varchar(104), md5_actual varchar(32), md5_original varchar(1024))");
			for (String line : lines) {
				if (!line.startsWith("#") && line.trim().length() > 0) {
					st.executeUpdate(line);
					print("Added line: " + line);
				}
			}

			st.executeUpdate("create table configs (config_name varchar(255), last_modified bigint, data blob)");
			String config = FileTools.readStringFromFile(new File("res/artifactory-config.xml"));
			in = new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8));

			ps = c.prepareStatement("insert into configs (config_name, last_modified, data) values ('artifactory.config.xml', ?, ?)");
			ps.setLong(1, (new Date()).getTime());
			ps.setBlob(2, in);
			ps.executeUpdate();

		} finally {
			IOTools.closeCloseable(ps, null);
			IOTools.closeCloseable(st, null);
			IOTools.closeCloseable(c, null);
			IOTools.closeCloseable(in, null);
		}
	}

	@Override
	protected void initializeServletEnvironment() {
		System.setProperty("RAVENHURST_REPOSITORY_TYPE", "artifactory");
	}

	@Test
	public void testScannerImplementation() throws Exception {
		RavenhurstContract beans = instance.servlet.context.contract();
		Scanner scanner = beans.scanner();
		assertThat(scanner).isInstanceOf(ArtifactoryScanner.class);
	}

	// @Test
	public void testScanner() throws Exception {

		RavenhurstContract beans = instance.servlet.context.contract();
		Scanner scanner = beans.scanner();
		assertThat(scanner).isNotNull();

		Long timestamp = scanner.getArtifactTimeStamp("archiva-clone", "ant", "ant-junit", "1.6.5");
		assertThat(timestamp).isEqualTo(1465487876000L);

		// Date d = new Date(1500901295999L);
		// Collection<String> artifacts = scanner.getChangedArtifacts("archiva-clone", d);
		// assertThat(artifacts).hasSize(1);
		// assertThat(artifacts.iterator().next()).isEqualTo("ant:ant#1.6.2");
		//
		// Collection<String> all = scanner.getChangedArtifacts("archiva-clone", null);
		// assertThat(all).hasSize(10);
	}

	@Test
	public void testVirtualRepositoryResolution() throws Exception {

		RavenhurstContract beans = instance.servlet.context.contract();
		Scanner scanner = beans.scanner();
		assertThat(scanner).isNotNull();

		Long cloneTimestamp = scanner.getArtifactTimeStamp("archiva-clone", "ant", "ant-junit", "1.6.5");
		Long virtualTimestamp = scanner.getArtifactTimeStamp("archiva-virtual", "ant", "ant-junit", "1.6.5");
		assertThat(cloneTimestamp).isEqualTo(1465487876000L);
		assertThat(virtualTimestamp).isEqualTo(1465487876000L);

		// Date d = new Date(1500901295999L);
		// Collection<String> cloneArtifacts = scanner.getChangedArtifacts("archiva-clone", d);
		// Collection<String> virtualArtifacts = scanner.getChangedArtifacts("archiva-virtual", d);
		// assertThat(cloneArtifacts).hasSize(1);
		// assertThat(cloneArtifacts.iterator().next()).isEqualTo("ant:ant#1.6.2");
		// assertThat(virtualArtifacts).hasSize(1);
		// assertThat(virtualArtifacts.iterator().next()).isEqualTo("ant:ant#1.6.2");
		//
		// Collection<String> cloneAll = scanner.getChangedArtifacts("archiva-clone", null);
		// Collection<String> virtualAll = scanner.getChangedArtifacts("archiva-virtual", null);
		// assertThat(cloneAll).hasSize(10);
		// assertThat(virtualAll).hasSize(10);
	}

	@Test
	public void testServlet() throws Exception {

		TestHttpServletRequest request = new TestHttpServletRequest("rest/archiva-clone/changes", asMap("timestamp", "2017-07-24T15:01:35.999+0200"));
		TestHttpServletResponse response = new TestHttpServletResponse();

		instance.servlet.doGet(request, response);

		assertThat(response.getResult()).isEqualTo("ant:ant#1.6.2");

		request = new TestHttpServletRequest("rest/archiva-clone/changes", null);
		response = new TestHttpServletResponse();

		instance.servlet.doGet(request, response);

		List<String> lines = StringTools.getLines(response.getResult());
		assertThat(lines).hasSize(10);

		request = new TestHttpServletRequest("rest/archiva-clone/timestamp", asMap("groupid", "ant", "artifactid", "ant-junit", "Version", "1.6.5"));
		response = new TestHttpServletResponse();

		instance.servlet.doGet(request, response);

		SimpleDateFormat sdf = new SimpleDateFormat(instance.servlet.context.contract().dateTimeFormat());
		Date expected = sdf.parse("2016-06-09T17:57:56.000+0200");
		Date actual = sdf.parse(response.getResult());
		assertThat(actual).isEqualTo(expected);
	}
}
