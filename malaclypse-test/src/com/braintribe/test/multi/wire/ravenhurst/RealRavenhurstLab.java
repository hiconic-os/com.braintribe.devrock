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
