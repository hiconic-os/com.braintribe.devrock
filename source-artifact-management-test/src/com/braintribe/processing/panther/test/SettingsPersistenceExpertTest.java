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
package com.braintribe.processing.panther.test;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.model.maven.settings.Settings;
import com.braintribe.model.panther.ArtifactRepository;
import com.braintribe.model.panther.SourceRepository;
import com.braintribe.model.processing.panther.depmgt.PublishWalkSettingsPersistenceExpert;


public class SettingsPersistenceExpertTest {

	@Test
	public void test() {
		SourceRepository sourceRepository = SourceRepository.T.create();
		sourceRepository.setName("publishing");
	
		
		ArtifactRepository repository = ArtifactRepository.T.create();
		repository.setName("braintribe_core");
		repository.setRepoUrl( "http://archiva.braintribe.com/repository/standalone");
		repository.setUser( "bla");
		repository.setPassword( "bla");
		repository.setUpdateReflectionUrl("http://archiva.braintribe.com/ravenhurst");
		
		sourceRepository.getLookupRepositories().add(repository);
		
		repository = ArtifactRepository.T.create();
		repository.setName("braintribe_dsc");
		repository.setRepoUrl( "http://artifactory.example.com/repository/standalone");
		repository.setUser( "blu");
		repository.setPassword( "blu2005");
		repository.setUpdateReflectionUrl("http://artifactory.example.com/ravenhurst");
		
		sourceRepository.getLookupRepositories().add(repository);
		
		repository = ArtifactRepository.T.create();
		repository.setName("tribefire");
		repository.setRepoUrl( "http://localhost:8080/repository");
		repository.setUser( "cortex");
		repository.setPassword( "cortex");
		
		sourceRepository.getLookupRepositories().add(repository);
		
		
		PublishWalkSettingsPersistenceExpert expert = new PublishWalkSettingsPersistenceExpert();
		expert.setSourceRepository(sourceRepository);
		expert.setLocalRepositoryLocation( "${M2_REPO}");
		
		try {
			Settings settings = expert.loadSettings();
			System.out.println(settings);
		} catch (RepresentationException e) {
			Assert.fail("exception [" + e + "] thrown");
		}
	}

}
