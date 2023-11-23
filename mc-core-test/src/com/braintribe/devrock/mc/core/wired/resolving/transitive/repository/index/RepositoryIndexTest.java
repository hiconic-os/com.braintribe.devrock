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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.repository.index;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.devrock.model.repository.filters.GroupsArtifactFilter;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.changes.ArtifactChanges;
import com.braintribe.model.artifact.changes.ArtifactIndexLevel;
import com.braintribe.model.artifact.changes.RepositoryProbeStatus;
import com.braintribe.model.artifact.changes.RepositoryProbingResult;
import com.braintribe.transport.http.DefaultHttpClientProvider;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.LazyInitialized;

/**
 * 
 * @author pit
 */
public class RepositoryIndexTest extends AbstractRepositoryIndexTest {
	protected LazyInitialized<CloseableHttpClient> httpClient = new LazyInitialized<>(this::client);
	
	private YamlMarshaller marshaller = new YamlMarshaller();

	@Test 
	public void test() throws Exception{
		// run initial		
		Pair<AnalysisArtifactResolution, RepositoryReflection> run = run( "com.braintribe.devrock.test:t#1.0.1", standardClasspathResolutionContext);
		
		// check filters etc
		List<String> grps = new ArrayList<>();
		grps.add("com.braintribe.devrock.test");
			
		// validate traces of first run 
		validate( repoCache, run.second, grps, 3, "1.0.1");
		
		// switch repolet content
		String url = "http://localhost:${port}/archive/update";
		url = url.replace("${port}", "" + launcher.getAssignedPort());
		try {
			CloseableHttpResponse response = getGetResponse( url + "?key=two");
			HttpEntity entity = response.getEntity();
			EntityUtils.consume(entity);
		} catch (IOException e) {	
			e.printStackTrace();
			Assert.fail("exception thrown while switching :" + e.getMessage());
		}			
		
		// run again 
		Pair<AnalysisArtifactResolution, RepositoryReflection> run2 = run( "com.braintribe.devrock.test:t#1.0.1", standardClasspathResolutionContext);
		
		// check filters etc
		grps.add( "com.braintribe.devrock.test.two");
	
		validate( repoCache, run2.second, grps, 6, "1.0.2");		
		
	}
	
	/**
	 * validates the footprint 
	 * @param repoCache - the {@link File} to the repo cache
	 * @param repoReflect - the repository reflection as retrieved
	 * @param filterContent - the expected content of the group filters
	 * @param sequenceNumber - the expected last sequence number of the artifact index
	 * @param indexVersion - the version of the artifact-index artifact
	 */
	private void validate(File repoCache, RepositoryReflection repoReflect, List<String> filterContent, int sequenceNumber, String indexVersion)  {
		Validator validator = new Validator();
		Repository repository = repoReflect.getRepository("archive");
		
		
		// a) check filters on repository  
		ArtifactFilter artifactFilter = repository.getArtifactFilter();		
		if (artifactFilter == null) {
			validator.assertTrue("no artifact filter attached to repository : archive", false);
		}
		else {
			if (artifactFilter instanceof GroupsArtifactFilter == false) {
				validator.assertTrue("artifact filter attached to repository [archive] is not a GroupArtifactFilter, but a :" + artifactFilter.getClass().getName(), false);
			}
			GroupsArtifactFilter gaf = (GroupsArtifactFilter) artifactFilter;
			Set<String> groups = gaf.getGroups();
			validateFilterContents(validator, "active group filter", filterContent, groups);
		}
						
		// b) check filters on disk - must be the samee content (duplicates removed)
		File grpFilterOnDiskFile = new File( repoCache, "group-index-archive.txt");
		
		if (!grpFilterOnDiskFile.exists()) {
			validator.assertTrue("group index file doesn't exist :" + grpFilterOnDiskFile.getAbsolutePath(), false);
		}
		else {
			try {
				String content  = IOTools.slurp(grpFilterOnDiskFile, "UTF-8");
				Set<String> values = new HashSet<>(Arrays.asList( content.split("\n")));
				validateFilterContents(validator, "persisted group filter", filterContent, values);
				
				
			} catch (IOException e) {
				validator.assertTrue("group index file cannot be read :" + grpFilterOnDiskFile.getAbsolutePath() + " as " + e.getMessage(), false);
			}			
		}
		
		
		// c) set last probing on disk 
		File lastChangesFile = new File( repoCache, "last-changes-access-archive.yaml");
		if (!lastChangesFile.exists()) {
			validator.assertTrue("last changes file doesn't exist :" + lastChangesFile.getAbsolutePath(), false);
		}
		else {
			try (InputStream in = new FileInputStream(lastChangesFile)) {
				ArtifactChanges ac = (ArtifactChanges) marshaller.unmarshall(in);
				///validator.assertTrue("url doesn't match. expected [" + repository.get, false)
				ac.getRepositoryUrl();
				ArtifactIndexLevel al = ac.getArtifactIndexLevel();
				Integer number = al.getSequenceNumber();
				validator.assertTrue("expected sequence# [" + sequenceNumber + "], found:" , sequenceNumber == number);
				String version = al.getVersion();
				validator.assertTrue("expected version# [" + indexVersion + "], found: " + version , indexVersion.compareTo( version) == 0);
			}
			catch (Exception e) {
				validator.assertTrue("last changes file cannot be read :" + lastChangesFile.getAbsolutePath() + " as " + e.getMessage(), false);				
			}
		}
		
		// d) last probing result
		File lastProbingResultFile = new File( repoCache, "last-probing-result-archive.yaml");
		if (!lastProbingResultFile.exists()) {
			validator.assertTrue("last probing result file doesn't exist :" + lastProbingResultFile.getAbsolutePath(), false);
		}
		else {
			try (InputStream in = new FileInputStream(lastProbingResultFile)) {
				RepositoryProbingResult rpr = (RepositoryProbingResult) marshaller.unmarshall(in);
				// must be status 'unprobed'
				RepositoryProbeStatus repositoryProbeStatus = rpr.getRepositoryProbeStatus();				
				validator.assertTrue("expected probing result to be [" + RepositoryProbeStatus.unprobed.name()  + "], found : " + repositoryProbeStatus.name(), repositoryProbeStatus == RepositoryProbeStatus.unprobed);				
			}
			catch (Exception e) {
				validator.assertTrue("last changes file cannot be read :" + lastProbingResultFile.getAbsolutePath() + " as " + e.getMessage(), false);
			}
		}
		validator.assertResults();
	}

	/**
	 * validate the contents of the group filters
	 * @param validator - the {@link Validator}
	 * @param tag - prefix for the string output
	 * @param filterContent - the expected content
	 * @param values - the acutal values
	 */
	private void validateFilterContents(Validator validator, String tag, Collection<String> filterContent, Set<String> values) {
		List<String> matches = new ArrayList<>();
		List<String> missing = new ArrayList<>();
		
		for (String suspect : filterContent) {
			if (values.contains(suspect)) {
				matches.add(suspect);
			}
			else {
				missing.add(suspect);
			}
		}
		List<String> excessive = new ArrayList<>( values);
		excessive.removeAll(matches);				
		
		validator.assertTrue(tag + " - missing groups: " + missing.stream().collect(Collectors.joining(",")), missing.size() == 0);
		validator.assertTrue(tag + " - excessive groups: " + missing.stream().collect(Collectors.joining(",")), excessive.size() == 0);
	}

	/**
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	protected CloseableHttpResponse getGetResponse(String url) throws IOException {
		HttpRequestBase requestBase = new HttpGet(url);
		HttpClientContext context = HttpClientContext.create();
		CloseableHttpResponse response = httpClient.get().execute(requestBase, context);
		return response;
	}
	/**
	 * @return
	 */
	protected CloseableHttpClient client() {
		DefaultHttpClientProvider bean = new DefaultHttpClientProvider();
		bean.setSocketTimeout(60_000);
		try {
			CloseableHttpClient httpClient = bean.provideHttpClient();
			return httpClient;
		} catch (Exception e) {
			throw new IllegalStateException("", e);
		}
	}
}
