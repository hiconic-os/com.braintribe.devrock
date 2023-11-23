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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.repository.probingstates;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.repolet.launcher.Launcher;

/**
 * tests that no failure are here if all's right
 *  
 * @author pit
 *
 */
public class RepositoryAllFineTest extends AbstractRepositoryConfigurationProbingTest {
	
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
				.close()

			.done();		
		return launcher;
	}

	@Test
	public void testUnauthorizedAccess() {
		try {
			RepositoryReflection repositoryReflection = getReflection();
			RepositoryConfiguration repositoryConfiguration = repositoryReflection.getRepositoryConfiguration();

			Validator validator = new Validator();
			
			validator.assertTrue("unexpectedly the repository-configuration is flagged as invalid", !repositoryConfiguration.hasFailed());
			
			for (Repository repository : repositoryConfiguration.getRepositories()) {
				validator.assertTrue("unexpectedly, repository [" + repository.getName() + "] is flagged as failed", !repository.hasFailed());
			}
			
			validator.assertResults();
					
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("unexpectedly an exception is thrown");
		}
		
	}
	
	

}
