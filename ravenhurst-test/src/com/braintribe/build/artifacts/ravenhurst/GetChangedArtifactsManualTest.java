package com.braintribe.build.artifacts.ravenhurst;

import java.sql.Date;

import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.build.artifacts.ravenhurst.wire.space.RavenhurstSpace;
import com.braintribe.build.ravenhurst.scanner.Scanner;
import com.braintribe.build.ravenhurst.scanner.ScannerException;

/**
 * A simple manual which just executes {@link Scanner#getChangedArtifacts(String, java.util.Date)}.
 * 
 * Respective environment variables or (or system properties) must be set. Examp,e:
 * 
 * -DRAVENHURST_REPOSITORY_TYPE=artifactory -DRAVENHURST_REPOSITORY_DB_DRIVER=com.mysql.cj.jdbc.Driver
 * -DRAVENHURST_REPOSITORY_DB_URL=jdbc:mysql://example-host:3306/artifactory?characterEncoding=UTF-8 -DRAVENHURST_REPOSITORY_DB_USER=example-user
 * -DRAVENHURST_REPOSITORY_DB_PASS=example-password
 */
@Ignore
public class GetChangedArtifactsManualTest {

	@Test
	public void test() throws ScannerException {
		Scanner scanner = new RavenhurstSpace().scanner();
		scanner.getChangedArtifacts("tribefire-3-0", Date.valueOf("2022-11-1"));
	}
}
