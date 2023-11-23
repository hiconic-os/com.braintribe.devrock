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
