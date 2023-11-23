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
package com.braintribe.devrock.mc.core.wired.repository.outdating;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.core.commons.RepositoryOutdater;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.wire.api.util.Lists;

/**
 * tests the 'repository index file outdater'.. mimicks a filled local repository which 3 different traces of repos
 * a) tests a named list of repos to 'outdate'
 * b) tests a 'wildcard' (regular expression) to 'outdate'
 * @author pit
 *
 */
public class RepositoryOutdaterTest implements HasCommonFilesystemNode {
	protected File repo;	
	protected File input;
	protected File output;	
	{	
		Pair<File,File> pair = filesystemRoots("repository/outdater");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");		
	}		
	protected File initial = new File( input, "initial");
		
	
	
	@Before
	public void before() {
		TestUtils.ensure( repo);
		
		if (initial.exists()) {
			TestUtils.copy(initial, repo);
		}
		
	}
	
	private List<String> getOutdatedMetadataFilesOfOutdatedMetadataFilesOf( String artifact, List<String> repos) {
		List<String> result = new ArrayList<>();
		File home = new File( repo, "com/braintribe/devrock/test/" + artifact);
		for (String repo : repos) {
			result.add( new File( home, "maven-metadata-" + repo + ".xml.outdated").getAbsolutePath().replace('\\','/'));
		}
		return result;
	}
	
	private List<String> getOutdatedPartAvailabilityFilesOf( String partial, String extension, List<String> repos) {
		List<String> result = new ArrayList<>();
		File home = new File( repo, "com/braintribe/devrock/test/" + partial);
		for (String repo : repos) {
			result.add( new File( home, "part-availability-" + repo + extension + ".outdated").getAbsolutePath().replace('\\','/'));
		}
		return result;
	}
	
	
	
	private List<String> getOutdatedFiles( File folder) {
		File [] files = folder.listFiles(new FileFilter() {		
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory())
					return true;
				String name = pathname.getName().toLowerCase();
				if (name.endsWith( ".outdated"))
					return true;				
				return false;
			}
		});
		List<String> result = new ArrayList<>();
		for (File file : files) {
			if (file.isDirectory()) {
				result.addAll( getOutdatedFiles(file));
			}
			else {
				String name = file.getAbsolutePath().replace('\\', '/');				
				result.add( name);
			}
		}
		return result;
	}
	
	/**
	 * tests out-dating with a list of repo names
	 */
	@Test 
	public void runRepositoryOutdaterWithNamedReposTest() {
		RepositoryOutdater rod = new RepositoryOutdater();
		rod.setLocalRepository(repo);
		rod.setRepositories( Lists.list("repo1", "repo2"));
		
		
		Reason reason = rod.outdateRepositories();
		
		// validate
		Validator validator = new Validator();
		validator.assertTrue("expected null reason, but got one", reason == null);
		if (reason != null) {
			validator.assertTrue("found reason:" + reason.stringify(), false);
		}
		// make sure all of 'repo1' and 'repo2' are outdated, yet not 'repo3'		 
		List<String> outdatedFiles = getOutdatedFiles(repo);
		
		List<String> expectations = new ArrayList<>();
		expectations.addAll( getOutdatedMetadataFilesOfOutdatedMetadataFilesOf("a", Lists.list("repo1", "repo2")));
		expectations.addAll( getOutdatedPartAvailabilityFilesOf("a/1.0.1", ".artifactory.json", Lists.list("repo1")));
		expectations.addAll( getOutdatedPartAvailabilityFilesOf("a/1.0.1", ".txt", Lists.list("repo2")));
		
		expectations.addAll( getOutdatedMetadataFilesOfOutdatedMetadataFilesOf("b", Lists.list("repo1", "repo2")));
		expectations.addAll( getOutdatedPartAvailabilityFilesOf("b/1.0.1", ".artifactory.json", Lists.list("repo1")));
		expectations.addAll( getOutdatedPartAvailabilityFilesOf("b/1.0.1", ".txt", Lists.list("repo2")));
		
		expectations.addAll( getOutdatedMetadataFilesOfOutdatedMetadataFilesOf("t", Lists.list("repo1", "repo2")));
		expectations.addAll( getOutdatedPartAvailabilityFilesOf("t/1.0.1", ".artifactory.json", Lists.list("repo1")));
		expectations.addAll( getOutdatedPartAvailabilityFilesOf("t/1.0.1", ".txt", Lists.list("repo2")));
		expectations.addAll( getOutdatedPartAvailabilityFilesOf("t/1.0.2", ".artifactory.json", Lists.list("repo1")));
		expectations.addAll( getOutdatedPartAvailabilityFilesOf("t/1.0.2", ".txt", Lists.list("repo2")));
		
		List<String> matches = new ArrayList<>();
		List<String> excess = new ArrayList<>();
		
		for (String outdated : outdatedFiles) {
			if (expectations.contains(outdated)) {
				matches.add(outdated);
			}
			else {
				excess.add(outdated);
			}
		}		
		validator.assertTrue("excess: " + excess.stream().collect( Collectors.joining(",")), excess.size() == 0);
		
		List<String> missing = new ArrayList<>( expectations);
		missing.removeAll(matches);
		validator.assertTrue("missing: " + missing.stream().collect( Collectors.joining(",")), missing.size() == 0);
		
		
		validator.assertResults();
		
	}
	
	/**
	 * tests out-dating with a wild card for the repo names
	 */
	@Test
	public void runRepositoryOutdaterWithWildCardTest() {
		RepositoryOutdater rod = new RepositoryOutdater();
		rod.setLocalRepository(repo);
		rod.setRepositories( Lists.list( ".*"));
		
		Reason reason = rod.outdateRepositories();		
		
		// validate
		Validator validator = new Validator();
		validator.assertTrue("expected null reason, but got one", reason == null);
		if (reason != null) {
			validator.assertTrue("found reason:" + reason.stringify(), false);
		}
	
		// make sure all of 'repo1', 'repo2' and 'repo3' are outdated
		List<String> outdatedFiles = getOutdatedFiles(repo);
		List<String> expectations = new ArrayList<>();
		expectations.addAll( getOutdatedMetadataFilesOfOutdatedMetadataFilesOf("a", Lists.list("repo1", "repo2", "repo3")));
		expectations.addAll( getOutdatedPartAvailabilityFilesOf("a/1.0.1", ".artifactory.json", Lists.list("repo1")));
		expectations.addAll( getOutdatedPartAvailabilityFilesOf("a/1.0.1", ".txt", Lists.list("repo2", "repo3")));
		
		expectations.addAll( getOutdatedMetadataFilesOfOutdatedMetadataFilesOf("b", Lists.list("repo1", "repo2", "repo3")));
		expectations.addAll( getOutdatedPartAvailabilityFilesOf("b/1.0.1", ".artifactory.json", Lists.list("repo1")));
		expectations.addAll( getOutdatedPartAvailabilityFilesOf("b/1.0.1", ".txt", Lists.list("repo2", "repo3")));
		
		expectations.addAll( getOutdatedMetadataFilesOfOutdatedMetadataFilesOf("t", Lists.list("repo1", "repo2", "repo3")));
		expectations.addAll( getOutdatedPartAvailabilityFilesOf("t/1.0.1", ".artifactory.json", Lists.list("repo1")));
		expectations.addAll( getOutdatedPartAvailabilityFilesOf("t/1.0.1", ".txt", Lists.list("repo2", "repo3")));
		expectations.addAll( getOutdatedPartAvailabilityFilesOf("t/1.0.2", ".artifactory.json", Lists.list("repo1")));
		expectations.addAll( getOutdatedPartAvailabilityFilesOf("t/1.0.2", ".txt", Lists.list("repo2", "repo3")));
		
		List<String> matches = new ArrayList<>();
		List<String> excess = new ArrayList<>();
		
		for (String outdated : outdatedFiles) {
			if (expectations.contains(outdated)) {
				matches.add(outdated);
			}
			else {
				excess.add(outdated);
			}
		}		
		validator.assertTrue("excess: " + excess.stream().collect( Collectors.joining(",")), excess.size() == 0);
		
		List<String> missing = new ArrayList<>( expectations);
		missing.removeAll(matches);
		validator.assertTrue("missing: " + missing.stream().collect( Collectors.joining(",")), missing.size() == 0);
		
		
		validator.assertResults();
	}
	

}
