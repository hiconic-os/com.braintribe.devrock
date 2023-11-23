// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.devrock.mc.core.wired.resolving.transitive.hashes;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.model.mc.reason.UnresolvedPart;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;

/**
 * tests how mc-core reacts on hashes 
 * 
 * a) missing hashes in header -> needs to request hash files 
 * b) wrong hashes -> needs to react on repository's checksum policy 
 * 
 * @author pit
 *
 */
public class HashesInDownloadTest extends AbstractHashTest {
	private static final String GRP = "com.braintribe.devrock.test";
	
	private static final String VRS = "1.0.1";
 
	
	
	
	@Override
	protected RepoletContent archiveInput() {		
		return archiveInput( "archive.definition.yaml");
	}
	
	private String buildDownloadExpectation( String artifactId, String part) {
		// /com/braintribe/devrock/test/a/1.0.1/a-1.0.1.pom
		return "/"+ GRP.replace('.', '/') + "/" +  artifactId + "/" + VRS + "/" + artifactId + "-" + VRS + "." + part;
	}

	/**
	 * simple regular download, kinda NOOP test
	 */
	@Test
	public void runHashesInDownloadMatchingHeadersTest() {
		CompiledPartIdentification cpi = CompiledPartIdentification.create(GRP, "t", VRS, "pom");		
		boolean present = runDownload( cpi);
		
		Assert.assertTrue( "expected [" + GRP + ":t#" + VRS + "] to exist, but it doesn't", present);		
		
		List<String> downloadedFiles = downloadsNotified.get("archive");		
		Assert.assertTrue("expected only one download event, yet found [" + downloadedFiles.size() + "]", downloadedFiles.size() == 1);		
		
		String downloadedFile = downloadedFiles.get( 0);
		String expectedDownload = buildDownloadExpectation("t", "pom");				
		Assert.assertTrue("expected the download to be [" + expectedDownload + "], but found [" + downloadedFile + "]", downloadedFile.equals(expectedDownload));
		
	}
	
	
	/**
	 * tests the fallback to the separate hash files is nothing is in the headers
	 */
	@Test
	public void runHashesInDownloadNoHeadersTest() {
		CompiledPartIdentification cpi = CompiledPartIdentification.create("com.braintribe.devrock.test", "a", "1.0.1", "pom");		
		boolean present = runDownload( cpi);
		
		
		Assert.assertTrue( "expected [" + GRP + ":t#" + VRS + "] to exist, but it doesn't", present);
		
		List<String> downloadedFiles = downloadsNotified.get("archive");
		
		Assert.assertTrue("expected only one download event, yet found [" + downloadedFiles.size() + "]", downloadedFiles.size() == 2);		
		
		// pom first
		String downloadedPom = downloadedFiles.get( 0);
		String expectedDownloadPom = buildDownloadExpectation("a", "pom");				
		Assert.assertTrue("expected the download to be [" + expectedDownloadPom + "], but found [" + downloadedPom + "]", downloadedPom.equals(expectedDownloadPom));
		
		
		// pom.md5 follows
		String downloadedHash = downloadedFiles.get( 1);
		String expectedDownloadHash = buildDownloadExpectation("a", "pom.md5");				
		Assert.assertTrue("expected the download to be [" + expectedDownloadHash + "], but found [" + downloadedHash + "]", downloadedHash.equals(expectedDownloadHash));		
	}
	
	/**
	 * tests the reaction if the hashes (in the headers) do not match -> should throw an exception (checksum policy is FAIL)
	 */
	@Test
	public void runHashesInDownloadNoMatchingHeadersTest() {
		CompiledPartIdentification cpi = CompiledPartIdentification.create(GRP, "b", VRS, "pom");
		Maybe<ArtifactDataResolution> resolutionMaybe = runDownloadReasoned( cpi);
		
		Assert.assertTrue("test should lead to an unresolved part but didn't", resolutionMaybe.isUnsatisfiedBy(UnresolvedPart.T));			
	}
}
