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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.model.artifactory.FileItem;
import com.braintribe.devrock.model.artifactory.FolderInfo;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.AbstractRepolet;
import com.braintribe.devrock.repolet.common.RepoletCommons;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.builder.cfg.LauncherCfg;
import com.braintribe.devrock.repolet.launcher.builder.cfg.RepoletCfg;
import com.braintribe.utils.lcd.LazyInitialized;

public class SimpleDescriptiveRepoletTest extends AbstractDescriptiveRepoletTest {
	private static final String ARCHIVE_NAME = "archive";
	private File configurationFile = new File( contents, "simpleTree.yaml");
	private LazyInitialized<RepoletContent> repoletContent = new LazyInitialized<>( this::loadContent);
	
	@Override
	protected RepoletContent getContent() {
		return repoletContent.get();
	}

	private RepoletContent loadContent() {		
		try {
			return RepositoryGenerations.unmarshallConfigurationFile(configurationFile);
		} catch (Exception e) {
			String msg = "exception thrown while unmarshalling setup file [" + configurationFile + "] as " + e.getLocalizedMessage();
			Assert.fail(msg);
			throw new IllegalStateException(msg, e);
		}
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
			foundRavenhurstResponses.putAll(retrieveRavenhurstResponse( rcfg.getName(), compiledChangesUrl));									
		}
		//
		List<String> expectedArtifacts = repoletContent.get().getArtifacts().stream().map( a -> a.asString()).collect( Collectors.toList());
		List<String> retrievedArtifacts = foundRavenhurstResponses.get( ARCHIVE_NAME);

		compareTwoStringResultLists(expectedArtifacts, retrievedArtifacts);				
	}

	private Map<String,List<String>> retrieveRavenhurstResponse(String repoName, String compiledChangesUrl) {
		Map<String, List<String>> foundRavenhurstResponses = new HashMap<>();
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
				foundRavenhurstResponses.put( repoName, result);
			}
			else {
				Assert.fail("unexpected status code [" + statusCode + "] while querying [" + repoName + "]");
			}
			
			EntityUtils.consume(response.getEntity());
							
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("exception thrown " + e.getMessage());
		
		}
		return foundRavenhurstResponses;
	}

	private void compareTwoStringResultLists(List<String> expected, List<String> found) {
		List<String> excess = new ArrayList<>();
		List<String> matching = new ArrayList<>();
		
		for (String retrieved : found) {
			if (expected.contains( retrieved)) {
				matching.add( retrieved);
			}
			else {
				excess.add( retrieved);
			}
		}
		List<String> missing = new ArrayList<>( expected);
		missing.removeAll( matching);
		
		Assert.assertTrue( "missing [" + missing.stream().collect(Collectors.joining(",")) + "]", missing.size() == 0);
		Assert.assertTrue( "excess [" + excess.stream().collect(Collectors.joining(",")) + "]", excess.size() == 0);
	}
	
	
	@Test
	public void testArtifactoryRestApi() {
		
		LauncherCfg cfg = launcher.getLaunchedCfg();
		Map<String,List<String>> foundRestResponses = new HashMap<>(cfg.getRepoletCfgs().size());		
		// 
		// 
		// 
		Map<String,String> repoletToRestQueryDirectory = new HashMap<>();
		String baseArtifact = "com/braintribe/devrock/test";
		repoletToRestQueryDirectory.put( ARCHIVE_NAME, baseArtifact);		
		
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
		List<String> expectedArtifacts = repoletContent.get().getArtifacts().stream().map( a -> a.getArtifactId()).collect( Collectors.toList());
		List<String> retrievedArtifacts = foundRestResponses.get( ARCHIVE_NAME);

		compareTwoStringResultLists(expectedArtifacts, retrievedArtifacts);				
	}

	
	/**
	 * uploads a file, checks whether it appears in RH answer, deletes the file again and checks whether it doesn't appear again in RH answer
	 * @param fileToUpload
	 */
	private void uploadAndDeleteTest(File fileToUpload) {		
		Path path = fileToUpload.toPath();
		Path uploadPath = uploads.toPath();
		Path relPath = uploadPath.relativize(path);
		String relPathAsString = relPath.toString().replace("\\", "/");		
				
		Map<String, String> launchedRepolets = launcher.getLaunchedRepolets();
		System.out.println();
		
		CloseableHttpClient client = client();
		HttpPut httpPut = new HttpPut( launchedRepolets.get( ARCHIVE_NAME) + "/" + relPathAsString);
		FileEntity fileEntity = new FileEntity( fileToUpload);
				
		Map<String, String> hashes = AbstractRepolet.generateHash( fileToUpload, Arrays.asList("sha1", "md5", "SHA-256"));		
		httpPut.setHeader("X-Checksum-Sha1", hashes.get("sha1"));
		httpPut.setHeader("X-Checksum-MD5", hashes.get("md5"));
		httpPut.setHeader("X-Checksum-SHA256", hashes.get("SHA-256"));
		
		httpPut.setEntity( fileEntity);
		int code = -1;
		try {
			CloseableHttpResponse response = client.execute(httpPut);
			code = response.getStatusLine().getStatusCode();
			response.close();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception [" + e.getMessage() + "] thrown");
			return;
		} 
		if (code != 200) {
			Assert.fail("unexpected return code [" + code + "] returned for HttpPut");
			return;
		}
		
		// validate uploaded file appears in RH answer 
		RepoletCfg rCfg = launcher.getCfgOfRepoletPerName( ARCHIVE_NAME);
		String compiledChangesUrl = rCfg.getChangesUrl().replace("${port}", "" + launcher.getAssignedPort());
		Map<String, List<String>> rhResponseAfterUpload = retrieveRavenhurstResponse(ARCHIVE_NAME, compiledChangesUrl);
		
		// find added file...
		Pair<String,String> expression = RepoletCommons.extractArtifactExpression( relPathAsString);
		List<String> rhResponseForRepolet = rhResponseAfterUpload.get( ARCHIVE_NAME);
		if (rhResponseForRepolet == null) {
			Assert.fail( "no RH information of archive [" + ARCHIVE_NAME + "] was retrieved");
			return;
		}
		boolean found = false;
		for (String art : rhResponseForRepolet) {
			if (art.equals( expression.first)) {
				found = true;
				break;
			}
		}
		if (!found) {
			Assert.fail("RH content doesn't contain [" + expression.first + "] which should have been created by uploading [" + fileToUpload.getAbsolutePath() + "]");
			return;
		}
		
		
		// delete file
		HttpDelete httpDelete =  new HttpDelete(launchedRepolets.get( ARCHIVE_NAME) + "/" + relPathAsString);
		code = -1;
		try {
			CloseableHttpResponse response = client.execute(httpDelete);
			code = response.getStatusLine().getStatusCode();
			response.close();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception [" + e.getMessage() + "] thrown");
			return;
		} 
		if (code != 200) {
			Assert.fail("unexpected return code [" + code + "] returned for HttpDelete");
			return;
		}	
		
		Map<String, List<String>> rhResponseAfterDelete = retrieveRavenhurstResponse(ARCHIVE_NAME, compiledChangesUrl);
		rhResponseForRepolet = rhResponseAfterDelete.get( ARCHIVE_NAME);
		if (rhResponseForRepolet == null) {
			Assert.fail( "no RH information of archive [" + ARCHIVE_NAME + "] was retrieved");
			return;
		}
		found = false;
		for (String art : rhResponseForRepolet) {
			if (art.equals( expression.first)) {
				found = true;
				break;
			}
		}
		if (found) {
			Assert.fail("RH content does contain [" + expression.first + "] which should have been removed by deleting [" + fileToUpload.getAbsolutePath() + "]");
			return;
		}
	}
	
	@Test
	public void uploadAndDeleteTest() {	
		uploadAndDeleteTest( new File( uploads, "com/braintribe/devrock/test/artifact/1.0/artifact-1.0.pom"));
	}		
	
	@Test
	public void download() { 
		
	}
	
}
