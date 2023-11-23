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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.repository.offline;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;

/**
 * test whether the SELECTIVE offline switch in a repository does set the correct repositories offline
 * 
 * actually, the magick happens at two places : 
 * 
 * com.braintribe.devrock.mc.core.wirings.resolver.space.ArtifactDataResolverSpace, line 341 ff
 * and
 * com.braintribe.devrock.mc.core.configuration.RepositoryConfigurationProbing, line line 112 ff 
 * 
 * test checks that only MavenHttpRepository types are offlined. 
 * note: actually, the correct way - into the future - would be to have a 'marker' interface that tells whether 
 * it's an offline-able repository. Of course, 404s lead to offlining, but that is done differently.
 * 
 * @author pit
 *
 */
public class SelectiveManualOfflineOnRepoConfig extends AbstractSelectiveRepositoryOfflineTest {
	
	

	
	@Override
	protected File config() {
		// TODO Auto-generated method stub
		return super.config();
	}

	@Test
	public void testGlobalOfflineInRepoConfig() {
		
		try {
			RepositoryReflection reflection = getReflection();
			//RepositoryConfiguration repositoryConfiguration = reflection.getRepositoryConfiguration();
			
			Validator validator = new Validator();
			
			// three repos expected 
			// install -> online
			validateRepository(validator, reflection, "install", true);
						
			// archive_1 -> offline
			validateRepository(validator, reflection, "archive_1", false);
			
			// archive_2 -> offline
			validateRepository(validator, reflection, "archive_2", false);
			
			validator.assertResults();
			
		} catch (Exception e1) {
			e1.printStackTrace();
			Assert.fail("Resolution failed with an exception");
		}
	}
	
	private void validateRepository( Validator validator, RepositoryReflection reflection, String repoName, boolean expectedOnline) {
		Repository repository = reflection.getRepository( repoName);			
		validator.assertTrue("no repository found : " + repoName, repository != null);
		if (repository != null) {
			validator.assertTrue("unexpectedly, repository is offline: " + repoName, !repository.getOffline() == expectedOnline);
		}	
	}
}
