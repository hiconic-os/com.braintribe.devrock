// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
package com.braintribe.devrock.test.repolet.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.repolet.folder.FolderBasedSwitchingRepolet;
import com.braintribe.devrock.repolet.launcher.builder.cfg.LauncherCfg;
import com.braintribe.devrock.repolet.launcher.builder.cfg.RepoletCfg;


/**
 * tests using the {@link FolderBasedSwitchingRepolet}
 * @author pit
 *
 */
public class FolderBasedSwitchingRepoletProbingTest extends AbstractFolderSwitchingBasedRepoletTest {
	private Map<String, List<String>> rhContentsBeforeSwitch;
	private Map<String,List<String>> rhContentsAfterSwitch;
	
	{
		rhContentsBeforeSwitch = new HashMap<>();
		rhContentsBeforeSwitch.put( "archive", Collections.singletonList( "com.braintribe.devrock.test:artifact#1.0"));
		
		rhContentsAfterSwitch = new HashMap<>();
		rhContentsAfterSwitch.put( "archive", Collections.singletonList( "com.braintribe.devrock.test:artifact#2.0"));
	}
	

	@Override
	protected File getRoot() {	
		return new File( res, "launcher");
	}


	
	private Map<String,List<String>> retrieveRavenhurstDump() {
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
		return foundRavenhurstResponses;		
	}
	
	@Test
	public void testSwitchingInitials() {
		LauncherCfg cfg = launcher.getLaunchedCfg();
		
		for (RepoletCfg rcfg : cfg.getRepoletCfgs()) {	
			Map<String, List<String>> map1 = retrieveRavenhurstDump();
			String url = "http://localhost:${port}/" + rcfg.getName() + "/update";
			url = url.replace("${port}", "" + cfg.getPort());
			try {
				CloseableHttpResponse response = getGetResponse( url + "?key=two");
				HttpEntity entity = response.getEntity();
				EntityUtils.consume(entity);
			} catch (IOException e) {	
				e.printStackTrace();
				Assert.fail("exception thrown while switching :" + e.getMessage());
			}			
			Map<String, List<String>> map2 = retrieveRavenhurstDump();
			
			validate(rhContentsBeforeSwitch.get( rcfg.getName()), map1.get( rcfg.getName()));
			validate(rhContentsAfterSwitch.get( rcfg.getName()), map2.get( rcfg.getName()));
		}
				
	}

}
