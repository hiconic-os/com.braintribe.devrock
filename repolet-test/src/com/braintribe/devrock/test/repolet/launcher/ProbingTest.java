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
package com.braintribe.devrock.test.repolet.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.model.artifactory.FileItem;
import com.braintribe.devrock.model.artifactory.FolderInfo;
import com.braintribe.devrock.repolet.folder.FolderBasedRepolet;
import com.braintribe.devrock.repolet.launcher.builder.cfg.LauncherCfg;
import com.braintribe.devrock.repolet.launcher.builder.cfg.RepoletCfg;

/**
 * tests using the {@link FolderBasedRepolet}
 * @author pit
 *
 */
public class ProbingTest extends AbstractFolderBasedRepoletTest{
	protected static final String changesUrlHeader = "X-Artifact-Repository-Changes-Url";
	protected static final String serverHeader = "Server";


	
	protected Map<String, List<String>> expectedInitialRavenhurstResponses;
	protected Map<Date, Map<String, List<String>>> expectedDatedRavenhurstResponses;
	protected Map<String, List<String>> expectedRestApiResponses;
	
	{
		expectedInitialRavenhurstResponses = new HashMap<>();
		
		expectedInitialRavenhurstResponses.put( "archiveA", Collections.singletonList("com.braintribe.devrock.test:artifact#1.0"));
		expectedInitialRavenhurstResponses.put( "archiveB", Collections.singletonList("com.braintribe.devrock.test:artifact#2.0"));
	
		expectedDatedRavenhurstResponses = new HashMap<>();
		
		Map<String,List<String>> mapForTestDate1 = new HashMap<>();
		mapForTestDate1.put("archiveA", Collections.singletonList("com.braintribe.devrock.test:answer1-artifact#1.0"));
		mapForTestDate1.put("archiveB", Collections.singletonList("com.braintribe.devrock.test:answer1-artifact#2.0"));		
		expectedDatedRavenhurstResponses.put( dateCodec.decode(testDate1AsString), mapForTestDate1);
		
		Map<String,List<String>> mapForTestDate2 = new HashMap<>();
		mapForTestDate2.put("archiveA", Collections.singletonList("com.braintribe.devrock.test:answer2-artifact#1.0"));
		mapForTestDate2.put("archiveB", Collections.singletonList("com.braintribe.devrock.test:answer2-artifact#2.0"));		
		expectedDatedRavenhurstResponses.put( dateCodec.decode(testDate2AsString), mapForTestDate2);
		
		Map<String,List<String>> mapForTestDate3 = new HashMap<>();
		mapForTestDate3.put("archiveA", Collections.singletonList("com.braintribe.devrock.test:answer3-artifact#1.0"));
		mapForTestDate3.put("archiveB", Collections.singletonList("com.braintribe.devrock.test:answer3-artifact#2.0"));		
		expectedDatedRavenhurstResponses.put( dateCodec.decode(testDate3AsString), mapForTestDate3);
		
		expectedRestApiResponses = new HashMap<>();
		List<String> expectedRestForA = Arrays.asList("artifact-1.0.pom", "artifact-1.0.jar", "artifact-1.0-sources.jar");
		expectedRestApiResponses.put("archiveA", expectedRestForA);
		List<String> expectedRestForB = Arrays.asList("artifact-2.0.pom", "artifact-2.0.jar", "artifact-2.0-sources.jar");
		expectedRestApiResponses.put( "archiveB", expectedRestForB);
		
	}
	
	
	@Override
	protected File getRoot() {	
		return new File( res, "launcher");
	}
	
	
	
	
	@Test
	public void testProbing() {
		LauncherCfg cfg = launcher.getLaunchedCfg();
		for (RepoletCfg rcfg : cfg.getRepoletCfgs()) {
			String name = rcfg.getName();
			String compiledUrl = launcher.getLaunchedRepolets().get( name);
			
			String declaredChangesUrl = rcfg.getChangesUrl().replace("${port}", ""+cfg.getPort());
			String declaredServerIdentification = rcfg.getServerIdentification();
			
			
			
			try {
				CloseableHttpResponse probingResponse = getOptionsResponse(compiledUrl);
				
				Header receivedChangesUlrheader = probingResponse.getLastHeader(changesUrlHeader);
				String foundChangesUlr = receivedChangesUlrheader.getValue();
				
				Assert.assertTrue("expected changes-url [" + declaredChangesUrl + "], yet found [" + foundChangesUlr + "]", declaredChangesUrl.equals(foundChangesUlr));

				Header receivedServerHeader = probingResponse.getLastHeader( serverHeader);
				String foundServerIdentification = receivedServerHeader.getValue();
				
				Assert.assertTrue("expected server-identification [" + declaredServerIdentification + "], yet found [" + foundServerIdentification + "]", declaredServerIdentification.equals(foundServerIdentification));
								
				EntityUtils.consume(probingResponse.getEntity());
								
			} catch (IOException e) {
				e.printStackTrace();
				Assert.fail("exception thrown");
			
			}									
		}
		
	}
	
	@Test
	public void testInitialRavenhurst() {
		LauncherCfg cfg = launcher.getLaunchedCfg();
		Map<String,List<String>> foundRavenhurstResponses = new HashMap<>(cfg.getRepoletCfgs().size());
		for (RepoletCfg rcfg : cfg.getRepoletCfgs()) {								
			String compiledChangesUrl = rcfg.getChangesUrl().replace("${port}", ""+cfg.getPort());								
			try {
				CloseableHttpResponse response = getGetResponse(compiledChangesUrl);
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode >= 200 && statusCode < 300) {
					HttpEntity entity = response.getEntity();
					List<String> result = new LinkedList<>();
					try ( BufferedReader reader = new BufferedReader( new InputStreamReader(entity.getContent(), "UTF-8"))) {				
						String line;
						while ((line = reader.readLine()) != null) 	{			
							result.add( line.trim());
						}						
					} catch (Exception e1) {
						; // TODO : leniency				
					}
					foundRavenhurstResponses.put( rcfg.getName(), result);
				}
				else {
					Assert.fail("unexpected status code [" + statusCode + "] while querying [" + rcfg.getName() + "]");
				}
				
				EntityUtils.consume(response.getEntity());
								
			} catch (IOException e) {
				e.printStackTrace();
				Assert.fail("exception thrown " + e.getMessage());
			
			}									
		}
		//
		for (Map.Entry<String, List<String>> entry : foundRavenhurstResponses.entrySet()) {
			List<String> expected = expectedInitialRavenhurstResponses.get( entry.getKey());
			Assert.assertTrue("null answer is unexpected for [" + entry.getKey() + "]", expected != null);
			validate( expected, entry.getValue());
		}
	}
	
	@Test
	public void testRavenhurst() {
		LauncherCfg cfg = launcher.getLaunchedCfg();
		Map<String,List<String>> foundRavenhurstResponses = new HashMap<>(cfg.getRepoletCfgs().size());
		String testDateAsString = "2020-02-28T10:10:33.836+0200";
		// 
		// 
		// 
		for (RepoletCfg rcfg : cfg.getRepoletCfgs()) {								
			String compiledChangesUrl = rcfg.getChangesUrl().replace("${port}", ""+cfg.getPort());
			try {
			compiledChangesUrl = compiledChangesUrl + RAVENHURST_PARAMETER + URLEncoder.encode(testDateAsString, "UTF-8");
			// add parameter & date stamp
				CloseableHttpResponse response = getGetResponse(compiledChangesUrl);
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode >= 200 && statusCode < 300) {
					HttpEntity entity = response.getEntity();
					List<String> result = new LinkedList<>();
					try ( BufferedReader reader = new BufferedReader( new InputStreamReader(entity.getContent(), "UTF-8"))) {				
						String line;
						while ((line = reader.readLine()) != null) 	{			
							result.add( line.trim());
						}						
					} catch (Exception e1) {
						; // TODO : leniency				
					}
					foundRavenhurstResponses.put( rcfg.getName(), result);
				}
				else {
					Assert.fail("unexpected status code [" + statusCode + "] while querying [" + rcfg.getName() + "]");
				}
				
				EntityUtils.consume(response.getEntity());
								
			} catch (IOException e) {
				e.printStackTrace();
				Assert.fail("exception thrown " + e.getMessage());
			
			}									
		}
		//
		Map<String,List<String>> datedExpectations = expectedDatedRavenhurstResponses.get(dateCodec.decode(testDate2AsString));
		for (Map.Entry<String, List<String>> entry : foundRavenhurstResponses.entrySet()) {
			
			List<String> expected = datedExpectations.get( entry.getKey());
			Assert.assertTrue("null answer is unexpected for [" + entry.getKey() + "]", expected != null);
			validate( expected, entry.getValue());
		}
	}
	
	@Test
	public void testArtifactoryRestApi() {
		
		LauncherCfg cfg = launcher.getLaunchedCfg();
		Map<String,List<String>> foundRestResponses = new HashMap<>(cfg.getRepoletCfgs().size());		
		// 
		// 
		// 
		Map<String,String> repoletToRestQueryDirectory = new HashMap<>();
		String baseArtifact = "com/braintribe/devrock/test/artifact";
		repoletToRestQueryDirectory.put( "archiveA", baseArtifact + "/1.0");
		repoletToRestQueryDirectory.put( "archiveB", baseArtifact + "/2.0");
		
		for (RepoletCfg rcfg : cfg.getRepoletCfgs()) {								
			String compiledRestApiUrl = rcfg.getRestApiUrl().replace("${port}", "" + cfg.getPort());
			String actualRestApiUrl = compiledRestApiUrl + "/" + repoletToRestQueryDirectory.get( rcfg.getName());

			try {
				CloseableHttpResponse response = getGetResponse(actualRestApiUrl);
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode >= 200 && statusCode < 300) {
					HttpEntity entity = response.getEntity();
					List<String> result = new LinkedList<>();					
					try (BufferedReader reader = new BufferedReader( new InputStreamReader( entity.getContent()))) {
						FolderInfo folderInfo = (FolderInfo) marshaller.unmarshall(reader, options);
						for (FileItem fileItem : folderInfo.getChildren()) {
							result.add( fileItem.getUri());
						}
					}
					entity.getContent();
					foundRestResponses.put( rcfg.getName(), result);
				}
				else {
					Assert.fail("unexpected status code [" + statusCode + "] while querying [" + rcfg.getName() + "]");
				}
				
				EntityUtils.consume(response.getEntity());
								
			} catch (IOException e) {
				e.printStackTrace();
				Assert.fail("exception thrown " + e.getMessage());
			
			}									
		}
		//

		for (Map.Entry<String, List<String>> entry : foundRestResponses.entrySet()) {			
			List<String> expected = expectedRestApiResponses.get( entry.getKey());
			Assert.assertTrue("null answer is unexpected for [" + entry.getKey() + "]", expected != null);
			validate( expected, entry.getValue());
		}
	}

	
	

}
