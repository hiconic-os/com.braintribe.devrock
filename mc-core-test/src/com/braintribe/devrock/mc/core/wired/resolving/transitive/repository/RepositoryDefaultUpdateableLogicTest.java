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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.repository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;

/**
 * tests the handling of the defaults in a repository configuration :
 * - if {@link Repository#getUpdateable()} is set (or missing), the repository
 * shouldn't have a 'null' {@link Repository#getUpdateTimeSpan()}, if not declared 
 * in the configuration.
 * 
 * @author pit
 *
 */
public class RepositoryDefaultUpdateableLogicTest extends AbstractRepositoryConfigurationCompilingTest  {
	private TimeSpan defaultTimespan = TimeSpan.create(1, TimeUnit.day);

	private List<Repository> expectedRepositories = new ArrayList<>();
	{
		
		// no flag in cfg, no update timespan
		Repository archiveImplicitlyUpdateable = Repository.T.createRaw();
		archiveImplicitlyUpdateable.setName("archive-implicitly-updating");
		archiveImplicitlyUpdateable.setUpdateable(true);
		archiveImplicitlyUpdateable.setUpdateTimeSpan( defaultTimespan);
		
		expectedRepositories.add(archiveImplicitlyUpdateable);
		
		// flag in cfg, no update timespan 
		Repository archiveExplicitlyUpdateable = Repository.T.createRaw();
		archiveExplicitlyUpdateable.setName("archive-updating");
		archiveExplicitlyUpdateable.setUpdateable(true);
		archiveExplicitlyUpdateable.setUpdateTimeSpan( defaultTimespan);
		
		expectedRepositories.add(archiveExplicitlyUpdateable);
		
		// negative flag in cfg, no update timespan
		Repository archiveExplicitlyNotUpdateable = Repository.T.createRaw();
		archiveExplicitlyNotUpdateable.setName("archive-non-updating");
		archiveExplicitlyNotUpdateable.setUpdateable(false);
	
		
		expectedRepositories.add(archiveExplicitlyNotUpdateable);	
	}
	
	protected RepoletContent archiveInput() {
		return RepoletContent.T.create();
	};
	
	@Override
	protected File config() {	
		return new File( input, "repository-configuration.yaml");
	}
	
	@Test 
	public void runUpdateableTest() {
		RepositoryReflection reflection;
		try {
			reflection = getReflection();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception thrown during reflection access");
			return;
		}
		
		List<String> missing = new ArrayList<>();
		List<String> matches = new ArrayList<>();
		List<String> misMatches = new ArrayList<>();
		for (Repository expectedRepository : expectedRepositories) {
			String expectedName = expectedRepository.getName();
			Repository foundRepository = reflection.getRepository(expectedName);
			if (foundRepository == null) {
				missing.add(expectedName);
			}
			else {
				boolean foundUpdateable = foundRepository.getUpdateable();
				if (foundUpdateable != expectedRepository.getUpdateable()) {
					misMatches.add( expectedName);
				}
				else {
					TimeSpan foundUpdateTimespan = foundRepository.getUpdateTimeSpan();
					if (foundUpdateable) {
						if (foundUpdateTimespan == null) {
							misMatches.add( expectedName);						
						}
						else if (defaultTimespan.compareTo( foundUpdateTimespan) == 0) {
							matches.add(expectedName);					
						}
						else { 
							misMatches.add( expectedName);
						}
					}
					else {
						matches.add( expectedName); // 
					}
				}
			}						
		}
		//
		List<String> excess = reflection.getRepositoryConfiguration().getRepositories().stream().map( r -> r.getName()).collect(Collectors.toList());
		excess.removeAll( matches);
		excess.removeAll( misMatches);
		excess.remove( "local");

		Validator validator = new Validator();

		validator.assertTrue("missing are [" + Validator.toString(missing) + "]", missing.size() == 0);
		validator.assertTrue("excess are [" + Validator.toString(excess) + "]", excess.size() == 0);
		validator.assertTrue("mismatches are [" + Validator.toString(misMatches) + "]", misMatches.size() == 0);
		
		validator.assertResults();
		
		
	}
	

}
