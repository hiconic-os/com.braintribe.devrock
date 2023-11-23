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
package com.braintribe.artifacts.test.version;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.version.Version;

public class VersionProcessorTest {
	private String versionAsString1 = "1.0.0";
	private String versionAsString2 = "1.1.3";
	private String versionAsString3 = "1.1.2";
	private String versionAsStringFuzzy3 = "1.1.^";
	
	private String versionAsString4Release = "3.0.5.RELEASE";
	private String versionAsString5 = "1.0";
	private String versionAsString5Snapshot = "1.0-SNAPSHOT";
	
	private String versionAsStringFinal = "1.1";
	private String versionAsStringMileStone1Dash = "1.1-M1";
	private String versionAsStringMileStone2Dash = "1.1-M2";
	private String versionAsStringMileStone1 = "1.1.M1";
	private String versionAsStringMileStone2 = "1.1.M2";
	
	private String versionAsStringReleaseCandidate1Dash = "1.1-rc1";
	private String versionAsStringReleaseCandidate2Dash = "1.1-rc2";
	private String versionAsStringReleaseCandidate1 = "1.1.rc1";
	private String versionAsStringReleaseCandidate2 = "1.1.rc2";
	
	private String versionAsFuckupedAsPossible1a = "2.1_3";
	private String versionAsFuckupedAsPossible1b = "2.1_4";
	private String versionAsFuckupedAsPossible2a = "2.0b4";
	private String versionAsFuckupedAsPossible2b = "2.0b5";
	
	private String versionAsStringHib1="3.5.4-Final";
	//private String versionAsStringHib2="3.5.5";
	
	private String versionAsStringGuava1 = "r05";
	private String versionAsStringGuava2 = "r06";
	
	private String versionAsStringFuckedTest = "fucked";

	private String simpleVersion = "1.9";
	private String simpleVersion2 = "1.9.1";
	
	@Test 
	public void testSimpleVersion() {
		try {
			Version version1 = VersionProcessor.createFromString( "1.9");
			Version version2 = VersionProcessor.createFromString( "2.0");
			
			Assert.assertTrue( "[" + "1.9" + "] is not less than [" + "2.0" + "]", VersionProcessor.isLess( version1, version2));
			Assert.assertTrue( "[" + "2.0" + "] is not higher than [" + "1.9" + "]", VersionProcessor.isHigher( version2, version1));
		} catch (VersionProcessingException e) {
			Assert.fail( "Exception ["+ e + "] thrown");
		}
	}
	
	@Test 
	public void testSimpleIndexedVersion() {
		try {
			Version version1 = VersionProcessor.createFromString( simpleVersion);
			Version version3 = VersionProcessor.createFromString( simpleVersion2);
			
			Assert.assertTrue( "[" + simpleVersion + "] is not less than [" + simpleVersion2 + "]", VersionProcessor.isLess( version1, version3));
			Assert.assertTrue( "[" + simpleVersion2 + "] is not higher than [" + simpleVersion + "]", VersionProcessor.isHigher( version3, version1));
		} catch (VersionProcessingException e) {
			Assert.fail( "Exception ["+ e + "] thrown");
		}
	}

	@Test
	public void testVersionFromString() {
		try {
			Version version = VersionProcessor.createFromString( "1.0.0");
			String versionAsString = VersionProcessor.toString(version);
			Assert.assertEquals( "1.0.0", versionAsString);
			
			version = VersionProcessor.createFromString( versionAsFuckupedAsPossible1a);
			versionAsString = VersionProcessor.toString(version);
			Assert.assertEquals( versionAsFuckupedAsPossible1a, versionAsString);
			
			version = VersionProcessor.createFromString( versionAsFuckupedAsPossible2a);
			versionAsString = VersionProcessor.toString(version);
			Assert.assertEquals( versionAsFuckupedAsPossible2a, versionAsString);
			
			version = VersionProcessor.createFromString( versionAsString4Release);
			versionAsString = VersionProcessor.toString(version);
			Assert.assertEquals( versionAsString4Release, versionAsString);
			
			version = VersionProcessor.createFromString( versionAsString5Snapshot);
			versionAsString = VersionProcessor.toString(version);
			Assert.assertEquals( versionAsString5Snapshot, versionAsString);
			
			version = VersionProcessor.createFromString( versionAsStringHib1);
			versionAsString = VersionProcessor.toString(version);
			Assert.assertEquals( versionAsStringHib1, versionAsString);
			
			version = VersionProcessor.createFromString( versionAsStringGuava1);
			versionAsString = VersionProcessor.toString(version);
			Assert.assertEquals( versionAsStringGuava1, versionAsString);
			
		
			
		} catch (VersionProcessingException e) {
			fail("Exception thrown " + e);
		}
	}

	@Test
	public void testIsFuzzy() {
		try {
			Version version = VersionProcessor.createFromString( versionAsStringFuzzy3);
			Assert.assertTrue( "version [" + versionAsStringFuzzy3 + "] isn't fuzzy", VersionProcessor.isFuzzy(version));
		} catch (VersionProcessingException e) {
			fail("Exception thrown " + e);
		}
	}

	@Test
	public void testRegexpFromVersion() {

	}

	@Test
	public void testIsLess() {
		try {
			Version version1 = VersionProcessor.createFromString( versionAsString1);
			Version version2 = VersionProcessor.createFromString( versionAsString2);
			Version version4 = VersionProcessor.createFromString( versionAsString4Release);
			Assert.assertTrue( "[" + versionAsString1 + "] is not less than [" + versionAsString2 + "]", VersionProcessor.isLess(version1, version2));
			Assert.assertTrue( "Reverse test [" + versionAsString1 + "] is less than [" + versionAsString2 + "]", !VersionProcessor.isLess(version2, version1));
			Assert.assertTrue( "[" + versionAsString1 + "] is higher than [" + versionAsString4Release + "]", VersionProcessor.isLess(version2, version4));
			
			// snapshot
			Version version5 = VersionProcessor.createFromString( versionAsString5);
			Version version6 = VersionProcessor.createFromString( versionAsString5Snapshot);
			Assert.assertTrue( "[" + versionAsString5 + "] is not less than [" + versionAsString5Snapshot + "]", VersionProcessor.isLess( version6, version5));
			
			// milestones
			Version versionFinal = VersionProcessor.createFromString( versionAsStringFinal);
			
			Version versionDottedMileStone1 = VersionProcessor.createFromString( versionAsStringMileStone1);
			Version versionDashedMileStone1 = VersionProcessor.createFromString( versionAsStringMileStone1Dash);
			
			Version versionDottedMileStone2 = VersionProcessor.createFromString( versionAsStringMileStone2);
			Version versionDashedMileStone2 = VersionProcessor.createFromString( versionAsStringMileStone2Dash);
			
			Version versionDottedReleaseCandidate1 = VersionProcessor.createFromString( versionAsStringReleaseCandidate1);
			Version versionDashedReleaseCandidate1 = VersionProcessor.createFromString( versionAsStringReleaseCandidate1Dash);
			
			Version versionDottedReleaseCandidate2 = VersionProcessor.createFromString( versionAsStringReleaseCandidate2);
			Version versionDashedReleaseCandidate2 = VersionProcessor.createFromString( versionAsStringReleaseCandidate2Dash);
			
			Assert.assertTrue( "[" + versionAsStringMileStone1 + "] is not less than [" + versionAsStringFinal + "]", VersionProcessor.isLess( versionDottedMileStone1, versionFinal));
			Assert.assertTrue( "[" + versionAsStringMileStone2 + "] is not less than [" + versionAsStringFinal + "]", VersionProcessor.isLess( versionDottedMileStone2, versionFinal));
			
			Assert.assertTrue( "[" + versionAsStringMileStone1Dash + "] is not less than [" + versionAsStringFinal + "]", VersionProcessor.isLess( versionDashedMileStone1, versionFinal));
			Assert.assertTrue( "[" + versionAsStringMileStone2Dash + "] is not less than [" + versionAsStringFinal + "]", VersionProcessor.isLess( versionDashedMileStone2, versionFinal));
			
			Assert.assertTrue( "[" + versionAsStringMileStone1Dash + "] is not less than [" + versionAsStringMileStone2Dash + "]", VersionProcessor.isLess( versionDashedMileStone1, versionDashedMileStone2));
			Assert.assertTrue( "[" + versionAsStringMileStone1 + "] is not less than [" + versionAsStringMileStone2 + "]", VersionProcessor.isLess( versionDottedMileStone1, versionDottedMileStone2));
			
			// release candidates
			Assert.assertTrue( "[" + versionAsStringReleaseCandidate1 + "] is not less than [" + versionAsStringFinal + "]", VersionProcessor.isLess( versionDottedReleaseCandidate1, versionFinal));
			Assert.assertTrue( "[" + versionAsStringReleaseCandidate2 + "] is not less than [" + versionAsStringFinal + "]", VersionProcessor.isLess( versionDottedReleaseCandidate2, versionFinal));
			
			Assert.assertTrue( "[" + versionAsStringReleaseCandidate1Dash + "] is not less than [" + versionAsStringFinal + "]", VersionProcessor.isLess( versionDashedReleaseCandidate1, versionFinal));
			Assert.assertTrue( "[" + versionAsStringReleaseCandidate2Dash + "] is not less than [" + versionAsStringFinal + "]", VersionProcessor.isLess( versionDashedReleaseCandidate2, versionFinal));
			
			Assert.assertTrue( "[" + versionAsStringReleaseCandidate1Dash + "] is not less than [" + versionAsStringReleaseCandidate2Dash + "]", VersionProcessor.isLess( versionDashedReleaseCandidate1, versionDashedReleaseCandidate2));
			Assert.assertTrue( "[" + versionAsStringReleaseCandidate1 + "] is not less than [" + versionAsStringReleaseCandidate2 + "]", VersionProcessor.isLess( versionDottedReleaseCandidate1, versionDottedReleaseCandidate2));
			
			// milestone vs release candidate
			Assert.assertTrue( "[" + versionAsStringMileStone1 + "] is not less than [" + versionAsStringReleaseCandidate1 + "]", VersionProcessor.isLess( versionDottedMileStone1, versionDottedReleaseCandidate1));
			
			// fuzzy
			Version fuzzy = VersionProcessor.createFromString( versionAsStringFuzzy3);
			Assert.assertTrue( "[" + versionAsStringFuzzy3 + "] is not less than [" + versionAsStringReleaseCandidate1 + "]", VersionProcessor.isLess( fuzzy, versionDottedReleaseCandidate1));
			
			
			// fucked up
			Version versionFuckedup1a = VersionProcessor.createFromString( versionAsFuckupedAsPossible1a);
			Version versionFuckedup1b = VersionProcessor.createFromString( versionAsFuckupedAsPossible1b);
			Assert.assertTrue( "[" + versionAsFuckupedAsPossible1a + "] is not less than [" + versionAsFuckupedAsPossible1b + "]", VersionProcessor.isLess( versionFuckedup1a, versionFuckedup1b));
			
			Version versionFuckedup2a = VersionProcessor.createFromString( versionAsFuckupedAsPossible2a);
			Version versionFuckedup2b = VersionProcessor.createFromString( versionAsFuckupedAsPossible2b);
			
			Assert.assertTrue( "[" + versionAsFuckupedAsPossible2a + "] is not less than [" + versionAsFuckupedAsPossible2b + "]", VersionProcessor.isLess( versionFuckedup2a, versionFuckedup2b));
			
			
			Version versionGuava1 = VersionProcessor.createFromString( versionAsStringGuava1);
			Version versionGuava2 = VersionProcessor.createFromString( versionAsStringGuava2);
			Assert.assertTrue( "[" + versionAsStringGuava1 + "] is not less than [" + versionAsStringGuava2 + "]", VersionProcessor.isLess( versionGuava1, versionGuava2));
			
			
			
		} catch (VersionProcessingException e) {
			fail("Exception thrown " + e);
		}
	}

	@Test
	public void testIsHigher() {
		try {
			Version version1 = VersionProcessor.createFromString( versionAsString1);
			Version version2 = VersionProcessor.createFromString( versionAsString2);
			Version version4 = VersionProcessor.createFromString( versionAsString4Release);
			Assert.assertTrue( "[" + versionAsString2 + "] is not higher than [" + versionAsString1 + "]", VersionProcessor.isHigher(version2, version1));
			Assert.assertTrue( "Reverse test: [" + versionAsString2 + "] is higher than [" + versionAsString1 + "]", !VersionProcessor.isHigher(version1, version2));
			Assert.assertTrue( "[" + versionAsString1 + "] is less than [" + versionAsString2 + "]", VersionProcessor.isHigher(version4, version1));
			
			Version version5 = VersionProcessor.createFromString( versionAsString5);
			Version version6 = VersionProcessor.createFromString( versionAsString5Snapshot);
			Assert.assertTrue( "[" + versionAsString5 + "] is not higher than [" + versionAsString5Snapshot + "]", VersionProcessor.isHigher(version5, version6));
			
			
			// milestones
			Version versionFinal = VersionProcessor.createFromString( versionAsStringFinal);
			
			Version versionDottedMileStone1 = VersionProcessor.createFromString( versionAsStringMileStone1);
			Version versionDashedMileStone1 = VersionProcessor.createFromString( versionAsStringMileStone1Dash);
			
			Version versionDottedMileStone2 = VersionProcessor.createFromString( versionAsStringMileStone2);
			Version versionDashedMileStone2 = VersionProcessor.createFromString( versionAsStringMileStone2Dash);
			
			Version versionDottedReleaseCandidate1 = VersionProcessor.createFromString( versionAsStringReleaseCandidate1);
			Version versionDashedReleaseCandidate1 = VersionProcessor.createFromString( versionAsStringReleaseCandidate1Dash);
			
			Version versionDottedReleaseCandidate2 = VersionProcessor.createFromString( versionAsStringReleaseCandidate2);
			Version versionDashedReleaseCandidate2 = VersionProcessor.createFromString( versionAsStringReleaseCandidate2Dash);
			
			Assert.assertFalse( "[" + versionAsStringMileStone1 + "] is not higher than [" + versionAsStringFinal + "]", VersionProcessor.isHigher( versionDottedMileStone1, versionFinal));
			Assert.assertFalse( "[" + versionAsStringMileStone2 + "] is not higher than [" + versionAsStringFinal + "]", VersionProcessor.isHigher( versionDottedMileStone2, versionFinal));
			
			Assert.assertFalse( "[" + versionAsStringMileStone1Dash + "] is not higher than [" + versionAsStringFinal + "]", VersionProcessor.isHigher( versionDashedMileStone1, versionFinal));
			Assert.assertFalse( "[" + versionAsStringMileStone2Dash + "] is not higher than [" + versionAsStringFinal + "]", VersionProcessor.isHigher( versionDashedMileStone2, versionFinal));
			
			Assert.assertFalse( "[" + versionAsStringMileStone1Dash + "] is not higher than [" + versionAsStringMileStone2Dash + "]", VersionProcessor.isHigher( versionDashedMileStone1, versionDashedMileStone2));
			Assert.assertFalse( "[" + versionAsStringMileStone1 + "] is not higher than [" + versionAsStringMileStone2 + "]", VersionProcessor.isHigher( versionDottedMileStone1, versionDottedMileStone2));
			
			
			Assert.assertFalse( "[" + versionAsStringReleaseCandidate1 + "] is not higher than [" + versionAsStringFinal + "]", VersionProcessor.isHigher( versionDottedReleaseCandidate1, versionFinal));
			Assert.assertFalse( "[" + versionAsStringReleaseCandidate2 + "] is not higher than [" + versionAsStringFinal + "]", VersionProcessor.isHigher( versionDottedReleaseCandidate2, versionFinal));
			
			Assert.assertFalse( "[" + versionAsStringReleaseCandidate1Dash + "] is not higher than [" + versionAsStringFinal + "]", VersionProcessor.isHigher( versionDashedReleaseCandidate1, versionFinal));
			Assert.assertFalse( "[" + versionAsStringReleaseCandidate2Dash + "] is not higher than [" + versionAsStringFinal + "]", VersionProcessor.isHigher( versionDashedReleaseCandidate2, versionFinal));
			
			Assert.assertFalse( "[" + versionAsStringReleaseCandidate1Dash + "] is not higher than [" + versionAsStringReleaseCandidate2Dash + "]", VersionProcessor.isHigher( versionDashedReleaseCandidate1, versionDashedReleaseCandidate2));
			Assert.assertFalse( "[" + versionAsStringReleaseCandidate1 + "] is not higher than [" + versionAsStringReleaseCandidate2 + "]", VersionProcessor.isHigher( versionDottedReleaseCandidate1, versionDottedReleaseCandidate2));
			
			// fucked up
			Version versionFuckedup1a = VersionProcessor.createFromString( versionAsFuckupedAsPossible1a);
			Version versionFuckedup1b = VersionProcessor.createFromString( versionAsFuckupedAsPossible1b);
			Assert.assertFalse( "[" + versionAsFuckupedAsPossible1a + "] is not less than [" + versionAsFuckupedAsPossible1b + "]", VersionProcessor.isHigher( versionFuckedup1a, versionFuckedup1b));
			
			Version versionFuckedup2a = VersionProcessor.createFromString( versionAsFuckupedAsPossible2a);
			Version versionFuckedup2b = VersionProcessor.createFromString( versionAsFuckupedAsPossible2b);
			
			Assert.assertFalse( "[" + versionAsFuckupedAsPossible2a + "] is not less than [" + versionAsFuckupedAsPossible2b + "]", VersionProcessor.isHigher( versionFuckedup2a, versionFuckedup2b));
			
			// milestone vs release candidate
			Assert.assertFalse( "[" + versionAsStringMileStone1 + "] is not less than [" + versionAsStringReleaseCandidate1 + "]", VersionProcessor.isHigher( versionDottedMileStone1, versionDottedReleaseCandidate1));
			
			// fuzzy
			Version fuzzy = VersionProcessor.createFromString( versionAsStringFuzzy3);
			Assert.assertFalse( "[" + versionAsStringFuzzy3 + "] is not less than [" + versionAsStringReleaseCandidate1 + "]", VersionProcessor.isHigher( fuzzy, versionDottedReleaseCandidate1));

			Version versionGuava1 = VersionProcessor.createFromString( versionAsStringGuava1);
			Version versionGuava2 = VersionProcessor.createFromString( versionAsStringGuava2);
			Assert.assertTrue( "[" + versionAsStringGuava2 + "] is not higher than [" + versionAsStringGuava1 + "]", VersionProcessor.isHigher( versionGuava2, versionGuava1));
			
			
		} catch (VersionProcessingException e) {
			fail("Exception thrown " + e);
		}
	}

	@Test
	public void testMatchesVersionVersion() {
		try {
			Version version1 = VersionProcessor.createFromString( versionAsString2);
			Version version3 = VersionProcessor.createFromString( versionAsStringFuzzy3);
			Version version4 = VersionProcessor.createFromString( versionAsString4Release);
			Assert.assertTrue( "[" + versionAsStringFuzzy3 + "] doesn't match than [" + versionAsString2 + "]", VersionProcessor.matches(version3, version1));
			Assert.assertTrue( "Reverse test: [" + versionAsString2 + "]  matches [" + versionAsStringFuzzy3 + "]", VersionProcessor.matches(version1, version3));
			Assert.assertFalse( "[" + versionAsStringFuzzy3 + "] doesn't match [" + versionAsString4Release + "]", VersionProcessor.matches(version3, version4));
			
			// fuzzy
			Version fuzzy = VersionProcessor.createFromString( versionAsStringFuzzy3);
			Version f1 = VersionProcessor.createFromString( versionAsString2);
			Version f2 = VersionProcessor.createFromString( versionAsString3);
			
			Version m1 = VersionProcessor.createFromString( versionAsStringMileStone1);
			Version r1 = VersionProcessor.createFromString( versionAsStringReleaseCandidate1);
			Version vfinal = VersionProcessor.createFromString( versionAsStringFinal);
			
			Assert.assertTrue( "[" + versionAsStringFuzzy3 + "] doesn't match [" + versionAsString2 + "]", VersionProcessor.matches(fuzzy, f1));
			Assert.assertTrue( "[" + versionAsStringFuzzy3 + "] doesn't match [" + versionAsString3 + "]", VersionProcessor.matches(fuzzy, f2));
			Assert.assertTrue( "[" + versionAsStringFuzzy3 + "] doesn't match [" + versionAsStringMileStone1 + "]", VersionProcessor.matches(fuzzy, m1));
			Assert.assertTrue( "[" + versionAsStringFuzzy3 + "] doesn't  match [" + versionAsStringReleaseCandidate1 + "]", VersionProcessor.matches(fuzzy, r1));
			Assert.assertTrue( "[" + versionAsStringFuzzy3 + "] doesn't  match [" + versionAsStringFinal + "]", VersionProcessor.matches(fuzzy, vfinal));
			
		} catch (VersionProcessingException e) {
			fail("Exception thrown " + e);
		}
	}

	@Test
	public void testMatchesVersionString() {
		try {
			Version version2 = VersionProcessor.createFromString( versionAsStringFuzzy3);		
			Assert.assertTrue( "[" + versionAsStringFuzzy3 + "] doesn't match [" + versionAsString2 + "] as String", VersionProcessor.matches(version2, versionAsString2));
			
			
		} catch (VersionProcessingException e) {
			fail("Exception thrown " + e);
		}
	}

	@Test
	public void testHardMatchesVersionVersion() {
		try {
			Version version1 = VersionProcessor.createFromString( versionAsString1);
			Version version2 = VersionProcessor.createFromString( versionAsString2);
			Assert.assertTrue( "[" + versionAsString2 + "] doesn't hard match [" + versionAsString1 + "]", VersionProcessor.hardMatches(version1, version1));
			Assert.assertTrue( "Reverse test: [" + versionAsString2 + "] hard matches [" + versionAsString1 + "]", !VersionProcessor.hardMatches(version1, version2));
			
			// fuzzy
			Version fuzzy = VersionProcessor.createFromString( versionAsStringFuzzy3);
			Version f1 = VersionProcessor.createFromString( versionAsString2);
			Version f2 = VersionProcessor.createFromString( versionAsString3);
			
			Version m1 = VersionProcessor.createFromString( versionAsStringMileStone1);
			Version r1 = VersionProcessor.createFromString( versionAsStringReleaseCandidate1);
			Version vfinal = VersionProcessor.createFromString( versionAsStringFinal);
			
			Assert.assertFalse( "[" + versionAsStringFuzzy3 + "] doesn't hard match [" + versionAsString2 + "]", VersionProcessor.hardMatches(fuzzy, f1));
			Assert.assertFalse( "[" + versionAsStringFuzzy3 + "] doesn't hard match [" + versionAsString3 + "]", VersionProcessor.hardMatches(fuzzy, f2));
			Assert.assertFalse( "[" + versionAsStringFuzzy3 + "] doesn't hard match [" + versionAsStringMileStone1 + "]", VersionProcessor.hardMatches(fuzzy, m1));
			Assert.assertFalse( "[" + versionAsStringFuzzy3 + "] doesn't hard match [" + versionAsStringReleaseCandidate1 + "]", VersionProcessor.hardMatches(fuzzy, r1));
			Assert.assertFalse( "[" + versionAsStringFuzzy3 + "] doesn't hard match [" + versionAsStringFinal + "]", VersionProcessor.hardMatches(fuzzy, vfinal));
			
			
		} catch (VersionProcessingException e) {
			fail("Exception thrown " + e);
		}
	}

	@Test
	public void testHardMatchesVersionString() {
		try {
			Version version1 = VersionProcessor.createFromString( versionAsString1);
			Assert.assertTrue( "[" + versionAsString1 + "] doesn't hard match [" + versionAsString1 + "]", VersionProcessor.hardMatches(version1, versionAsString1));
			Assert.assertTrue( "Reverse test : [" + versionAsString2 + "] hard matches [" + versionAsString1 + "]", VersionProcessor.hardMatches(version1, versionAsString1));
			
			// fuzzy 
			
		} catch (VersionProcessingException e) {
			fail("Exception thrown " + e);
		}
	}

	@Test
	public void testIsUndefined() {
		try {
			Version version = VersionProcessor.createVersion();
			Assert.assertTrue( "Version's not undefined", VersionProcessor.isUndefined(version));
			Version version1 = VersionProcessor.createFromString( versionAsString1);
			Assert.assertTrue( "Reverse test : Version's undefined", !VersionProcessor.isUndefined(version1));
		} catch (VersionProcessingException e) {
			fail("Exception thrown " + e);
		}
		
	}
	
	@Test 
	public void testIsFucked() {
		try {
			Version fuckedVersion = VersionProcessor.createFromString( versionAsStringFuckedTest);
			Version correctVersion = VersionProcessor.createFromString( versionAsString5);
			Assert.assertFalse( "[" + versionAsStringFuckedTest + "] is not greater than [" + versionAsString5 + "]", VersionProcessor.isHigher(fuckedVersion, correctVersion));
			Assert.assertFalse( "[" + versionAsStringFuckedTest + "] is not smaller than [" + versionAsString5 + "]", VersionProcessor.isLess(fuckedVersion, correctVersion));
		} catch (VersionProcessingException e) {
			fail("Exception thrown " + e);
		}
	}
	
	
	@Test 
	public void testPublishCandidates() {
		try {
			Version baseVersion = VersionProcessor.createFromString( "1.0");
			Version pc1Version = VersionProcessor.createFromString( "1.0.1-pc1");
			Version pc2Version = VersionProcessor.createFromString( "1.0.1-pc2");
			Version publishedVersion = VersionProcessor.createFromString( "1.0.1");
			
			Assert.assertTrue( "base version is not smaller than pc1 version", VersionProcessor.isLess(baseVersion, pc1Version));
			Assert.assertTrue( "pc1 version is not smaller than pc2 version", VersionProcessor.isLess(pc1Version, pc2Version));
			Assert.assertTrue( "pc2 version is not smaller than published version", VersionProcessor.isLess(pc2Version, publishedVersion));

			Assert.assertTrue( "pc1 version is not higher than base version", VersionProcessor.isHigher(pc1Version, baseVersion));
			Assert.assertTrue( "pc2 version is not higher than pc1 version", VersionProcessor.isHigher(pc2Version, pc1Version));
			Assert.assertTrue( "published version is not higher than pc2 version", VersionProcessor.isHigher(publishedVersion, pc2Version));

		
		} catch (VersionProcessingException e) {
			fail("Exception thrown " + e);
		}
	}

	@Test
	public void testFuzzyPublish()  {
		try {
			Version pc1Version = VersionProcessor.createFromString( "1.0.1-pc1");
			Version pc2Version = VersionProcessor.createFromString( "1.0.1-pc2");
			Version fuzzyVersion = VersionProcessor.createFromString( "1.0.^");
			
			Assert.assertTrue( "fuzzy version doesn't match pc1 version", VersionProcessor.matches( fuzzyVersion, pc1Version));
			Assert.assertTrue( "fuzzy version doesn't match pc2 version", VersionProcessor.matches( fuzzyVersion, pc2Version));
		} catch (VersionProcessingException e) {
			fail("Exception thrown " + e);
		}
	}
	

	@Test
	public void testMajorMinorToHotfix() {
		try {
			Version pc1Version = VersionProcessor.createFromString( "1.0.0");
			Version pc2Version = VersionProcessor.createFromString( "1.0");
			
			
			Assert.assertTrue( "1.0.0 matches 1.0", !VersionProcessor.matches( pc1Version, pc2Version));
			Assert.assertTrue( "1.0 matches 1.0.0", !VersionProcessor.matches( pc2Version, pc1Version));
		} catch (VersionProcessingException e) {
			fail("Exception thrown " + e);
		}
		
	}
	
	@Test
	public void testMajorMinorCategorization() {
		
		List<Version> versions = new ArrayList<>();
		
		versions.add( VersionProcessor.createFromString( "1.0.2-pc"));
		versions.add( VersionProcessor.createFromString( "1.0.1"));
		versions.add( VersionProcessor.createFromString( "1.0.2"));
		versions.add( VersionProcessor.createFromString( "1.0.3"));
		
		Version top_1_0 = VersionProcessor.createFromString( "1.0.4");
		versions.add( top_1_0);		

		versions.add( VersionProcessor.createFromString( "1.1.1"));
		
		Version top_1_1 = VersionProcessor.createFromString( "1.1.2");
		versions.add( top_1_1);
		
		
		Version top_1_2 = VersionProcessor.createFromString( "1.2.1-pc");
		versions.add( top_1_2);
		
		
		List<Version> winners = VersionProcessor.filterMajorMinorWinners(versions);
		
		Assert.assertTrue( "excpected 3 winners, yet found [" + winners.size() + "]", winners.size() == 3);
		Assert.assertTrue("1.0.4 expected but not found", winners.contains( top_1_0));
		Assert.assertTrue("1.1.2 expected but not found", winners.contains( top_1_1));
		Assert.assertTrue("1.2.1-pc. expected but not found", winners.contains( top_1_2));
		
	}

}
