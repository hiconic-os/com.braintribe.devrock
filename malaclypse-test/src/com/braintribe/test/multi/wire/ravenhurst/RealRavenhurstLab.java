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
package com.braintribe.test.multi.wire.ravenhurst;

import org.junit.Test;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationClient;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationClientFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationException;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstRequest;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstResponse;
import com.braintribe.test.multi.wire.AbstractWalkerWireTest;

public class RealRavenhurstLab extends AbstractWalkerWireTest {

	
	
	@Test
	public void test() {
		
		RepositoryInterrogationClientFactoryImpl interrogationClientFactory = new RepositoryInterrogationClientFactoryImpl();
		RavenhurstBundle bundle = RavenhurstBundle.T.create();		
		bundle.setRavenhurstClientKey("https");
		RavenhurstRequest request = RavenhurstRequest.T.create();
		request.setUrl( "https://artifactory.example.com/Ravenhurst/rest/devrock///changes/?timestamp=2019-09-18T13%3A36%3A57.642%2B0200");
		bundle.setRavenhurstRequest(request);
		
		RepositoryInterrogationClient repositoryInterrogationClient = interrogationClientFactory.apply( bundle);		
		try {
			RavenhurstResponse ravenhurstResponse = repositoryInterrogationClient.interrogate( bundle.getRavenhurstRequest());
			System.out.println( ravenhurstResponse.getResponseDate());
		} catch (RepositoryInterrogationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
