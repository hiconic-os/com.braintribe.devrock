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
package com.braintribe.marshaller.maven.metadata.test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;

import com.braintribe.model.artifact.maven.meta.MavenMetaData;
import com.braintribe.model.artifact.maven.meta.Snapshot;
import com.braintribe.model.artifact.maven.meta.SnapshotVersion;
import com.braintribe.model.artifact.maven.meta.Versioning;
import com.braintribe.model.version.Version;

/**
 * a validator for {@link MavenMetaData}, i.e. can compare to {@link MavenMetaData} instances 
 * @author pit
 *
 */
public class MavenMetadataValidator {
	
	
	/**
	 * simple validate that either both are null or both are non-null
	 * @param tag - the tag for the assert message (denoting the property)
	 * @param found - the found {@link Object}
	 * @param expected - the expected {@link Object}
	 * @return
	 */
	private static boolean validateNonNull(String tag, Object found, Object expected) {
		if (found != null && expected == null) {
			Assert.fail( tag + ": expected [null], found [" + found + "]");
			return false;
		}
		if (found == null && expected != null) {
			Assert.fail( tag + ": expected [" + expected + "], found [null]");
			return false;
		}
		return true;
	}
	
	/**
	 * validate two comparables 
	 * @param tag - the tag for the assert message (denoting the property)
	 * @param found - the found {@link Comparable}
	 * @param expected - the expected {@link Comparable}
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static boolean matchComparable( String tag, Comparable found, Comparable expected) {
		if (validateNonNull(tag, found, expected)) {
			if (expected != null) {
				boolean condition = expected.compareTo(found) == 0;
				Assert.assertTrue( tag + ": expected [" + expected + "], found [" + found + "]", condition);
				return condition;			
			}
			return true;
		}
		return false;
	}
	
	/**
	 * validate string value 
	 * @param tag - the tag for the assert message (denoting the property)
	 * @param found - the found {@link String} value
	 * @param expected - the expected {@link String} value
	 * @return - true if matches
	 */
	private static boolean matchString( String tag, String found, String expected) {
		if (validateNonNull(tag, found, expected)) {
			if (expected != null) {
			boolean condition = expected.equalsIgnoreCase( found);
			Assert.assertTrue( tag + ": expected [" + expected + "], found [" + found + "]", condition);
			return condition;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * validate two {@link Version}
	 * @param tag - the tag for the assert message (denoting the property)
	 * @param found - the found {@link Version}
	 * @param expected - the expected {@link Version}
	 * @return - true if matches 
	 */
	private static boolean matchVersion( String tag, Version found, Version expected) {
		if (validateNonNull(tag, found, expected)) {
			if (expected != null) {
				boolean condition  = expected.compareTo(found) == 0;
				Assert.assertTrue( tag + ": expected [" + expected + "], found [" + found + "]", condition);
				return condition;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * collates versions into a comma delimited sttring 
	 * @param versions - the {@link List} of {@link Version}
	 * @return - a comma delimited string of the {@link Version}'s string representation 
	 */
	private static String collate( List<Version> versions) {
		return versions.stream().map( v -> v.asString()).collect( Collectors.joining(","));
	}
	
	/**
	 * checks if passed list of {@link Version} contains the passed {@link Version}
	 * @param versions - the {@link List} of {@link Version}
	 * @param e - the {@link Version} to look for
	 * @return - true if contained, false otherwise 
	 */
	private static boolean contains( List<Version> versions, Version e) {
		return versions.stream().filter( v -> {
			return v.compareTo( e) == 0;
		}).findFirst().orElse( null) != null;		
	}
	
	/**
	 * validates the {@link Versioning}
	 * @param found - the found {@link Versioning}
	 * @param expected - the expected {@link Versioning}
	 * @return - true if valid, false otherwise 
	 */
	private static boolean validateVersioning( Versioning found, Versioning expected) {
		String tag = "versioning";
		if (!validateNonNull(tag, found, expected))
			return false;
		
		if (expected == null)
			return true;
		
		boolean passed = true;
		// simple versions
		List<Version> foundVersions = found.getVersions();
		List<Version> expectedVersions = expected.getVersions();
		if (!validateNonNull(tag, foundVersions, expectedVersions)) {
			passed = false;			
		}
		else {
			if (foundVersions != null) {				
				List<Version> matched = new ArrayList<>();
				List<Version> excess = new ArrayList<>();
				foundVersions.stream().forEach( v -> {					
					if (contains( expectedVersions, v)) {
						matched.add( v);
					}
					else {
						excess.add( v);
					}
				});
				List<Version> missing = new ArrayList<>();
				missing.addAll( expectedVersions);
				missing.removeAll( matched);
				
				Assert.assertTrue( "versions don't match expection : [excess  " + collate( excess) + "], missing [" + collate(missing) + "]", matched.size() == expectedVersions.size());
			}
		}
		
		
		// snapshot
		Snapshot foundSnapshot = found.getSnapshot();
		Snapshot expectedSnapshot = expected.getSnapshot();
		if (!validateNonNull(tag, foundSnapshot, expectedSnapshot)) {
			passed = false;
		}
		else {
			if (expectedSnapshot != null) {
				matchComparable( "timestamp", foundSnapshot.getTimestamp(), expectedSnapshot.getTimestamp());
				matchComparable( "buildNumber", Integer.valueOf(foundSnapshot.getBuildNumber()), Integer.valueOf(expectedSnapshot.getBuildNumber()));
				Assert.assertTrue( "localcopy : expected [" + expectedSnapshot.getLocalCopy() + "], found [" + foundSnapshot.getLocalCopy() + "]", expectedSnapshot.getLocalCopy() == foundSnapshot.getLocalCopy());
			}
		}
		// snapshot-versions
		List<SnapshotVersion> foundSnapshotVersions = found.getSnapshotVersions();
		List<SnapshotVersion> expectedSnapshotVersions = expected.getSnapshotVersions();
		if (!validateNonNull(tag, foundSnapshotVersions, expectedSnapshotVersions)) {
			passed = false;			
		}
		else {
			validateSnapshotVersions( foundSnapshotVersions, expectedSnapshotVersions);
		}
		return passed;
		
	}
	
	/**
	 * matches (rather tests on equality) two {@link SnapshotVersion}
	 * @param found - found {@link SnapshotVersion}
	 * @param expected - expected {@link SnapshotVersion}
	 * @return - true if matches 
	 */
	private static boolean matches( SnapshotVersion found, SnapshotVersion expected) {
		 
		if (!matchString("extension", found.getExtension(), expected.getExtension())) 
			return false;
		
		if (!matchString("value", found.getValue(), expected.getValue())) 
			return false;
		
		if (!matchString("classifier", found.getExtension(), expected.getExtension())) 
			return false;
		
		return true;
	}
	
	/**
	 * checks whether the passed {@link List} contains the passed {@link SnapshotVersion}
	 * @param snapshotVersions - a {@link List} of {@link SnapshotVersion}
	 * @param e - the {@link SnapshotVersion} to check 
	 * @return - true if contained, false otherwise 
	 */
	private static boolean contains( List<SnapshotVersion> snapshotVersions, SnapshotVersion e) {
		return snapshotVersions.stream().filter( sv -> {
			return matches( sv, e);
		}).findFirst().orElse( null) != null;
	}
	
	/**
	 * collates {@link SnapshotVersion}s so that they can be printed in an assertion message.
	 * @param svs - the {@link List} of {@link SnapshotVersion}
	 * @return - the a comma-delimited {@link String} of the {@link SnapshotVersion}'s string representation  
	 */
	private static String collateSv( List<SnapshotVersion> svs) {
		return svs.stream().map( sv -> {
			return sv.getValue() + "." + "-" + sv.getClassifier() + "." + sv.getExtension();
		}).collect( Collectors.joining(","));
	}
	
	/**
	 * @param foundSnapshotVersions
	 * @param expectedSnapshotVersions
	 * @return
	 */
	private static boolean validateSnapshotVersions(List<SnapshotVersion> foundSnapshotVersions, List<SnapshotVersion> expectedSnapshotVersions) {
		boolean passed = true;
		// simple versions		
		if (!validateNonNull("snapshotVersions", foundSnapshotVersions, expectedSnapshotVersions)) {
			passed = false;			
		}
		else {
			if (foundSnapshotVersions != null) {				
				List<SnapshotVersion> matched = new ArrayList<>();
				List<SnapshotVersion> excess = new ArrayList<>();
				foundSnapshotVersions.stream().forEach( v -> {					
					if (contains( expectedSnapshotVersions, v)) {
						matched.add( v);
					}
					else {
						excess.add( v);
					}
				});
				List<SnapshotVersion> missing = new ArrayList<>();
				missing.addAll( expectedSnapshotVersions);
				missing.removeAll( matched);
				
				Assert.assertTrue( "versions don't match expection : [excess  " + collateSv( excess) + "], missing [" + collateSv(missing) + "]", matched.size() == expectedSnapshotVersions.size());
			}
		}
		return passed;
		
	}

	/**
	 * validates (aka compares) two {@link MavenMetaData} instances, and asserts it  
	 * @param found - the {@link MavenMetaData} as found
	 * @param expected - the {@link MavenMetaData} as expected
	 */
	public static void validate( MavenMetaData found, MavenMetaData expected) {
		matchString( "groupId", found.getGroupId(), expected.getGroupId());
		matchString( "artifactId", found.getArtifactId(), expected.getArtifactId());
		matchVersion( "version", found.getVersion(), expected.getVersion());
		
		validateVersioning( found.getVersioning(), expected.getVersioning());
	}
		
}
