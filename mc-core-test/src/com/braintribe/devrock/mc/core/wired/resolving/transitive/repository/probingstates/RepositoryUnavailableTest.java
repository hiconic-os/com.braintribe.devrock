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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.repository.probingstates;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.model.mc.reason.configuration.RepositoryUnavailable;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.repolet.launcher.Launcher;

/**
 * tests that unauthorized repository : 403
 *  
 * @author pit
 *
 */
public class RepositoryUnavailableTest extends AbstractRepositoryConfigurationProbingTest {
	
	@Override
	protected Launcher launcher() {
		Launcher launcher = Launcher.build()
				.repolet()
				.name("archive")
					.descriptiveContent()
						.descriptiveContent(archiveInput())
					.close()
				.close()
								
				.repolet()
				.name("failing-archive")
					.descriptiveContent()
						.descriptiveContent(archiveInput())
					.close()
					.overridingResponseCode(404)
				.close()

			.done();		
		return launcher;
	}

	@Test
	public void testUnauthorizedAccess() {
		try {
			RepositoryReflection repositoryReflection = getReflection();
			RepositoryConfiguration repositoryConfiguration = repositoryReflection.getRepositoryConfiguration();

			validate( repositoryConfiguration, "failing-archive", RepositoryUnavailable.T);
			
					
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("unexpectedly an exception is thrown");
		}
		
	}
	
	

}
