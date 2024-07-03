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
