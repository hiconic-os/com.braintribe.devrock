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
package com.braintribe.devrock.mc.core.compiler;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;

import com.braintribe.devrock.mc.core.configuration.maven.MavenSettingsCompiler;
import com.braintribe.devrock.model.repository.ChecksumPolicy;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repository.filters.ArtifactFilter;
import com.braintribe.devrock.model.repository.filters.QualifiedArtifactFilter;
import com.braintribe.model.time.TimeSpan;

/**
 * a validator for {@link RepositoryConfiguration}s as returned by the {@link MavenSettingsCompiler}
 * @author pit
 *
 */
public interface RepositoryConfigurationValidator {	
	
	/**
	 * @param tag - a string to tag the output 
	 * @param expected - the expected value 
	 * @param found - the found value
	 */
	static void validate(String tag, String expected, String found) {
		if (expected != null) {
			Assert.assertTrue( tag + ": expected [" + expected + "], found [" + found + "]", expected.equals( found));
		}
		else {
			Assert.assertTrue( tag + ": expected [null], found [" + found + "]", found == null);
		}	
	}
	
	static String toString( TimeSpan span) {
		return "" + span.getValue() + " " + span.getUnit();
	}
	
	/**
	 * @param tag - tag
	 * @param expected - the expected {@link TimeSpan}
	 * @param found - the found {@link TimeSpan}
	 */
	static void validate( String tag, TimeSpan expected, TimeSpan found) {
		if (expected != null) {
			if (found == null) {
				Assert.fail( tag + ": expected [" + toString(expected) + "], found [null]");
			}
			else {
				Assert.assertTrue( tag + ": expected [" + toString(expected) + "], found [" + toString(found) + "]", expected.getUnit() == found.getUnit() && expected.getValue() == found.getValue());
			}
		}
		else {
			Assert.assertTrue( tag + ": expected [null], found [" + found + "]", found == null);
		}
	}
	
	/**
	 * @param tag
	 * @param expected
	 * @param found
	 */
	static void validate( String tag, ChecksumPolicy expected, ChecksumPolicy found) {
		if (expected != null) {
			if (found == null) {
				Assert.fail( tag + ": expected [" + expected.toString() + "], found [null]");
			}
			else {
				Assert.assertTrue( tag + ": expected [" + expected.toString() + "], found [" + found.toString() + "]", expected == found);
			}
		}
		else {
			Assert.assertTrue( tag + ": expected [null], found [" + found + "]", found == null);
		}
	}
	
		
	/**
	 * @param expected - the expected {@link Repository}
	 * @param found - the found {@link Repository}
	 */
	static void validate( Repository expected, Repository found) {
		// name is identical ..
		validate("name", expected.getName(), found.getName());
		Assert.assertTrue("snapshot flag : expected [" + expected.getSnapshotRepo() + "], found [" + found.getSnapshotRepo() + "]", expected.getSnapshotRepo() == found.getSnapshotRepo());
		String mainTag = "[" + expected.getName() + "(" + (expected.getSnapshotRepo() ? "snapshot" : "release") + ")]";
		
		
		if (expected instanceof MavenHttpRepository && found instanceof MavenHttpRepository) {
			MavenHttpRepository r1 = (MavenHttpRepository) expected;
			MavenHttpRepository r2 = (MavenHttpRepository) found;
			
			validate( mainTag + " url", r1.getUrl(), r2.getUrl());
			validate( mainTag + " user", r1.getUser(), r2.getUser());
			validate( mainTag + " password", r1.getPassword(), r2.getPassword());
			validate( mainTag + " update policy", r1.getUpdateTimeSpan(), r2.getUpdateTimeSpan());
			validate( mainTag + " checksum policy", r1.getCheckSumPolicy(), r2.getCheckSumPolicy());
		}
	}
	
	/**
	 * @param expected - the expected {@link RepositoryConfiguration}
	 * @param found - the found {@link RepositoryConfiguration}
	 */
	static void validate( RepositoryConfiguration expected, RepositoryConfiguration found) {
		if (expected == null) {
			System.out.println("no expectation, no validation");
			return;
		}
		// local 
		validate( "local repo", expected.getLocalRepositoryPath(), found.getLocalRepositoryPath());
		
		List<Repository> relevantRepositories = found.getRepositories();
		
		int size = expected.getRepositories().size();
		Assert.assertTrue( "expected [" + size + "] repositories, found [" + relevantRepositories.size() + "] " + found.getRepositories().stream().map( r -> r.getName()).collect( Collectors.joining(",")), relevantRepositories.size() == size);
		
		for (Repository foundR : relevantRepositories) {
			String id = foundR.getName();	
			boolean isSnapshot = foundR.getSnapshotRepo();
			Repository expectedR = expected.getRepositories().stream().filter( r -> r.getName().equals( id) && r.getSnapshotRepo() == isSnapshot).findFirst().orElse(null);
			if (expectedR == null) {
				Assert.fail("unexpected respository [" + id + "] found");
			}					
			else {
				validate( expectedR, foundR);
			}
			ArtifactFilter foundFilter = foundR.getArtifactFilter();
			ArtifactFilter expectedFilter = expectedR.getArtifactFilter();
			
			if (expectedFilter == null) {
				if (foundFilter != null) {
					Assert.fail("unexpected artifact filter for repository [" + id + "] found");
				}				
			}
			else {
				if (foundFilter == null) {
					Assert.fail("expected artifact filter not found for repository [" + id + "] found");
				}
				else {
					validate( foundFilter, expectedFilter);
				}
			}
		}
	}

	static void validate(ArtifactFilter foundFilter, ArtifactFilter expectedFilter) {
		String foundSig = foundFilter.entityType().getTypeSignature();
		String expectedSig = expectedFilter.entityType().getTypeSignature();
		
		Assert.assertTrue("expected filter type [" + expectedSig + "] but found [" + foundSig + "]", expectedSig.equals( foundSig));
		
		switch( foundSig) {
			case "com.braintribe.devrock.model.repository.filters.QualifiedArtifactFilter": {
				QualifiedArtifactFilter foundQaFilter = (QualifiedArtifactFilter) foundFilter;
				QualifiedArtifactFilter expectedQaFilter = (QualifiedArtifactFilter) expectedFilter;				
				validate( foundQaFilter, expectedQaFilter);
			}
		}		
	}
	
	static void validate( QualifiedArtifactFilter foundFilter, QualifiedArtifactFilter expectedFilter) {
		Assert.assertTrue( "expected group-id [" + foundFilter.getGroupId() + "], found [" + expectedFilter.getGroupId() + "]", foundFilter.getGroupId().equals( expectedFilter.getGroupId()));
		Assert.assertTrue( "expected artifact-id [" + foundFilter.getArtifactId() + "], found [" + expectedFilter.getArtifactId() + "]", foundFilter.getArtifactId().equals( expectedFilter.getArtifactId()));
	}
	
}
