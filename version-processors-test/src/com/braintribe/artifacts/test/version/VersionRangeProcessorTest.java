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
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;

public class VersionRangeProcessorTest {
	
	String versionAsLowerBounds = "1.1.0";
	Version versionLowerBounds = null;
	
	String versionAsUpperBounds  = "1.1.9";
	Version versionUpperBounds = null;
	
	String versionAsTooHigh  = "2.0";
	Version versionTooHigh = null;
	
	String versionAsTooLow  = "1.0.0";
	Version versionTooLow = null;
	
	String versionAsInBetween = "1.1.5";
	Version versionInBetween = null;
	
	String versionRangeDirect = "1.1.0";
	
	String versionRangeOpenLower = "[1.1.0,1.1.9)";
	String versionRangeOpenUpper = "(1.1.0,1.1.9]";
	String versionRangeOpen = "[1.1.0,1.1.9]";
	String versionRangeClosed = "(1.1.0,1.1.9)";
	String versionRangeClosedLower = "(1.1.0,1.1.9]";
	String versionRangeClosedUpper = "[1.1.0,1.1.9)";
	
	String versionUndefinedUpperRange = "[1.1.0,]";
	String versionUndefinedLowerRange = "[,1.1.9]";
	
	
	
	String springTestVersion1 = "2.5.6";
	String springTestVersion2 = "3.0.5.RELEASE";
	String springTestRange = "[3.0,]";
	

	String beanShellRange1 = "2.0b4";
	String beanShellRange2 = "2.0b5";
	String beanShellRange3 = "2.1b1";
	String beanShellRange4 = "2.1";
	
	String rangeMetricsTestRangeUpperClosed = "[1.0,2.0)";
	String rangeMetricsTestRangeUpperOpen = "[1.0,2.0]";
	
	String rangeMetricsTestRange2 = "1.0";
	String rangeMetricsTestRange3 = "2.0";
	
	String rangeMetricsTestRangeLowerClosed = "(1.0,2.0]";
	String rangeMetricsTestRangeLowerOpen = "[1.0,2.0]";

	String rangeMetricsTestRangeLowerUndefined = "[,2.0]";
	String rangeMetricsTestRangeUpperUndefined = "[1.0,]";
	
	

	@Before
	public void setUp() throws Exception {		
		versionLowerBounds = VersionProcessor.createFromString( versionAsLowerBounds);
		versionUpperBounds = VersionProcessor.createFromString( versionAsUpperBounds);
		versionTooHigh = VersionProcessor.createFromString( versionAsTooHigh);
		versionTooLow = VersionProcessor.createFromString( versionAsTooLow);
		versionInBetween = VersionProcessor.createFromString( versionAsInBetween);
	}

	
	@Test
	public void testVersionRangeConversion() {
		try {
			
			VersionRange range = VersionRangeProcessor.createFromString( versionRangeDirect);
			String versionRangeDirectString = VersionRangeProcessor.toString( range);
			Assert.assertEquals( "[" + versionRangeDirect + "] doesn't match ["+ versionRangeDirectString + "]", versionRangeDirect, versionRangeDirectString);
			
			range = VersionRangeProcessor.createFromString( versionRangeOpenLower);
			String versionRangeOpenLowerString = VersionRangeProcessor.toString( range);			
			Assert.assertEquals( "[" + versionRangeOpenLower + "] doesn't match ["+ versionRangeOpenLowerString + "]", versionRangeOpenLower, versionRangeOpenLowerString);
			
			range = VersionRangeProcessor.createFromString( versionRangeOpenUpper);
			String versionRangeOpenUpperString = VersionRangeProcessor.toString( range);
			Assert.assertEquals( "[" + versionRangeOpenUpper + "] doesn't match ["+ versionRangeOpenUpperString + "]", versionRangeOpenUpper, versionRangeOpenUpperString);
			
			range = VersionRangeProcessor.createFromString( versionUndefinedLowerRange);
			String versionUndefinedLowerRangeString = VersionRangeProcessor.toString( range);
			Assert.assertEquals( "[" + versionUndefinedLowerRange + "] doesn't match ["+ versionUndefinedLowerRangeString + "]", versionUndefinedLowerRange, versionUndefinedLowerRangeString);
			
			range = VersionRangeProcessor.createFromString( versionUndefinedUpperRange);
			String versionUndefinedUpperRangeString = VersionRangeProcessor.toString( range);
			Assert.assertEquals( "[" + versionUndefinedUpperRange + "] doesn't match ["+ versionUndefinedUpperRangeString + "]", versionUndefinedUpperRange, versionUndefinedUpperRangeString);
			
			range = VersionRangeProcessor.createFromString( versionRangeClosed);
			String versionRangeClosedString = VersionRangeProcessor.toString( range);
			Assert.assertEquals( "[" + versionRangeClosed + "] doesn't match ["+ versionRangeClosedString + "]", versionRangeClosed, versionRangeClosedString);
			
			range = VersionRangeProcessor.createFromString( versionRangeOpen);
			String versionRangeOpenString = VersionRangeProcessor.toString( range);
			Assert.assertEquals( "[" + versionRangeOpen + "] doesn't match ["+ versionRangeOpenString + "]", versionRangeOpen, versionRangeOpenString);
			
					
			
		} catch (VersionProcessingException e) {
			fail( "Exception thrown");
		}
		
	}

	@Test
	public void testHardMatchesVersionRangeVersion() {
		try {
			VersionRange range = VersionRangeProcessor.createFromString( versionRangeDirect);
			Assert.assertTrue( "[" + VersionRangeProcessor.toString(range) + "] doesn't hard match [" + VersionProcessor.toString(versionLowerBounds)+ "]", VersionRangeProcessor.hardMatches( range, versionLowerBounds));
			
			range = VersionRangeProcessor.createFromString( versionRangeOpen);
			Assert.assertTrue( "[" + VersionRangeProcessor.toString(range) + "]  hard matches [" + VersionProcessor.toString(versionTooLow)+ "]", !VersionRangeProcessor.hardMatches( range, versionTooLow));
		} catch (VersionProcessingException e) {
			fail("Exception thrown :" + e);
		}
		
	}

	@Test
	public void testMatchesVersionRangeVersion() {
		try {
			VersionRange range = VersionRangeProcessor.createFromString( versionRangeDirect);
			Assert.assertTrue( "[" + VersionRangeProcessor.toString(range) + "] doesn't match [" + VersionProcessor.toString(versionLowerBounds)+ "]", VersionRangeProcessor.hardMatches( range, versionLowerBounds));
			
			range = VersionRangeProcessor.createFromString( versionRangeOpen);
			Assert.assertFalse( "[" + VersionRangeProcessor.toString(range) + "] matches [" + versionAsTooLow + "]", VersionRangeProcessor.hardMatches( range, versionTooLow));
			Assert.assertFalse( "[" + VersionRangeProcessor.toString(range) + "]  matches [" + versionAsTooHigh + "]", VersionRangeProcessor.hardMatches( range, versionTooHigh));
			Assert.assertTrue( "[" + VersionRangeProcessor.toString(range) + "]  doesn't match [" + versionAsLowerBounds + "]", VersionRangeProcessor.hardMatches( range, versionLowerBounds));			
			Assert.assertTrue( "[" + VersionRangeProcessor.toString(range) + "]  doesn't match [" + versionAsUpperBounds + "]", VersionRangeProcessor.hardMatches( range, versionUpperBounds));
			Assert.assertTrue( "[" + VersionRangeProcessor.toString(range) + "]  doesn't match [" + versionAsInBetween + "]", VersionRangeProcessor.hardMatches( range, versionInBetween));
			
				
			range = VersionRangeProcessor.createFromString(versionRangeClosed);
			Assert.assertFalse( "[" + VersionRangeProcessor.toString(range) + "]  matches [" + versionAsTooLow + "]", VersionRangeProcessor.hardMatches( range, versionTooLow));
			Assert.assertFalse( "[" + VersionRangeProcessor.toString(range) + "]  matches [" + versionAsTooHigh + "]", VersionRangeProcessor.hardMatches( range, versionTooHigh));
			Assert.assertFalse( "[" + VersionRangeProcessor.toString(range) + "]  matches [" + versionAsLowerBounds + "]", VersionRangeProcessor.hardMatches( range, versionLowerBounds));			
			Assert.assertFalse( "[" + VersionRangeProcessor.toString(range) + "]  matches [" + versionAsUpperBounds + "]", VersionRangeProcessor.hardMatches( range, versionUpperBounds));			
			Assert.assertTrue( "[" + VersionRangeProcessor.toString(range) + "]  doesn't match [" + versionAsInBetween + "]", VersionRangeProcessor.hardMatches( range, versionInBetween));			
			
			range = VersionRangeProcessor.createFromString( versionRangeClosedUpper);
			Assert.assertFalse( "[" + VersionRangeProcessor.toString(range) + "]  matches [" + versionAsTooLow + "]", VersionRangeProcessor.hardMatches( range, versionTooLow));
			Assert.assertFalse( "[" + VersionRangeProcessor.toString(range) + "]  matches [" + versionAsTooHigh + "]", VersionRangeProcessor.hardMatches( range, versionTooHigh));
			Assert.assertTrue( "[" + VersionRangeProcessor.toString(range) + "]  doesn't match [" + versionAsLowerBounds + "]", VersionRangeProcessor.hardMatches( range, versionLowerBounds));			
			Assert.assertFalse( "[" + VersionRangeProcessor.toString(range) + "]  matches [" + versionAsUpperBounds + "]", VersionRangeProcessor.hardMatches( range, versionUpperBounds));			
			Assert.assertTrue( "[" + VersionRangeProcessor.toString(range) + "]  doesn't match [" + versionAsInBetween + "]", VersionRangeProcessor.hardMatches( range, versionInBetween));
			
			range = VersionRangeProcessor.createFromString( versionRangeClosedLower);
			Assert.assertFalse( "[" + VersionRangeProcessor.toString(range) + "]  matches [" + versionAsTooLow + "]", VersionRangeProcessor.hardMatches( range, versionTooLow));
			Assert.assertFalse( "[" + VersionRangeProcessor.toString(range) + "]  matches [" + versionAsTooHigh + "]", VersionRangeProcessor.hardMatches( range, versionTooHigh));
			Assert.assertFalse( "[" + VersionRangeProcessor.toString(range) + "]  matches [" + versionAsLowerBounds + "]", VersionRangeProcessor.hardMatches( range, versionLowerBounds));			
			Assert.assertTrue( "[" + VersionRangeProcessor.toString(range) + "]  doesn't match [" + versionAsUpperBounds + "]", VersionRangeProcessor.hardMatches( range, versionUpperBounds));			
			Assert.assertTrue( "[" + VersionRangeProcessor.toString(range) + "]  doesn't match [" + versionAsInBetween + "]", VersionRangeProcessor.hardMatches( range, versionInBetween));
			
			
			range = VersionRangeProcessor.createFromString( versionRangeOpenUpper);			
			Assert.assertFalse( "[" + VersionRangeProcessor.toString(range) + "] matches [" + versionAsTooLow + "]", VersionRangeProcessor.hardMatches( range, versionTooLow));
			Assert.assertFalse( "[" + VersionRangeProcessor.toString(range) + "]  matches [" + versionAsTooHigh + "]", VersionRangeProcessor.hardMatches( range, versionTooHigh));
			Assert.assertFalse( "[" + VersionRangeProcessor.toString(range) + "]  matches [" + versionAsLowerBounds + "]", VersionRangeProcessor.hardMatches( range, versionLowerBounds));			
			Assert.assertTrue( "[" + VersionRangeProcessor.toString(range) + "]  doesn't match [" + versionAsUpperBounds + "]", VersionRangeProcessor.hardMatches( range, versionUpperBounds));			
			Assert.assertTrue( "[" + VersionRangeProcessor.toString(range) + "]  doesn't match [" + versionAsInBetween + "]", VersionRangeProcessor.hardMatches( range, versionInBetween));			
			
			range = VersionRangeProcessor.createFromString( versionRangeOpenLower);			
			Assert.assertFalse( "[" + VersionRangeProcessor.toString(range) + "]  matches [" + versionAsTooLow + "]", VersionRangeProcessor.hardMatches( range, versionTooLow));
			Assert.assertFalse( "[" + VersionRangeProcessor.toString(range) + "]  matches [" + versionAsTooHigh + "]", VersionRangeProcessor.hardMatches( range, versionTooHigh));
			Assert.assertTrue( "[" + VersionRangeProcessor.toString(range) + "]  doesn't match [" + versionAsLowerBounds + "]", VersionRangeProcessor.hardMatches( range, versionLowerBounds));			
			Assert.assertFalse( "[" + VersionRangeProcessor.toString(range) + "]  matches [" + versionAsUpperBounds + "]", VersionRangeProcessor.hardMatches( range, versionUpperBounds));			
			Assert.assertTrue( "[" + VersionRangeProcessor.toString(range) + "]  doesn't match [" + versionAsInBetween + "]", VersionRangeProcessor.hardMatches( range, versionInBetween));
			
			
			range = VersionRangeProcessor.createFromString( versionUndefinedLowerRange);
			Assert.assertTrue( "[" + VersionRangeProcessor.toString(range) + "]  doesn't match [" + versionAsTooLow + "]", VersionRangeProcessor.hardMatches( range, versionTooLow));
			Assert.assertFalse( "[" + VersionRangeProcessor.toString(range) + "]  matches [" + versionAsTooHigh + "]", VersionRangeProcessor.hardMatches( range, versionTooHigh));			
			Assert.assertTrue( "[" + VersionRangeProcessor.toString(range) + "]  doesn't match [" + versionAsInBetween + "]", VersionRangeProcessor.hardMatches( range, versionInBetween));
			
			
			range = VersionRangeProcessor.createFromString( versionUndefinedUpperRange);
			Assert.assertFalse( "[" + VersionRangeProcessor.toString(range) + "]  matches [" + versionAsTooLow + "]", VersionRangeProcessor.hardMatches( range, versionTooLow));
			Assert.assertTrue( "[" + VersionRangeProcessor.toString(range) + "]  doesn't match [" + versionAsTooHigh + "]", VersionRangeProcessor.hardMatches( range, versionTooHigh));			
			Assert.assertTrue( "[" + VersionRangeProcessor.toString(range) + "]  doesn't match [" + versionAsInBetween + "]", VersionRangeProcessor.hardMatches( range, versionInBetween));
			
		} catch (VersionProcessingException e) {
			fail("Exception thrown " + e);
		}
		
	}
	
	@Test
	public void testMatchesVersionRangeVersionSpringTest() {
		try {
			VersionRange range = VersionRangeProcessor.createFromString( springTestVersion2);
			Version version1 = VersionProcessor.createFromString(springTestVersion1);
			Assert.assertTrue( "[" + VersionRangeProcessor.toString(range) + "] matches [" + VersionProcessor.toString(version1)+ "]", !VersionRangeProcessor.hardMatches( range, version1));
			
			
			VersionRange range2 = VersionRangeProcessor.createFromString( springTestRange);
			Assert.assertTrue( "[" + VersionRangeProcessor.toString(range2) + "] matches [" + VersionProcessor.toString(version1)+ "]", !VersionRangeProcessor.hardMatches( range2, version1));
			
			Version version2 = VersionProcessor.createFromString(springTestVersion2);
			Assert.assertTrue( "[" + VersionRangeProcessor.toString(range2) + "] doesn't match [" + VersionProcessor.toString(version2)+ "]", VersionRangeProcessor.hardMatches( range2, version2));
			
		} catch (VersionProcessingException e) {
			fail("Exception thrown " + e);
		}
		
	}

	@Test
	public void testMatchesVersionRangeString() {
		try {
			VersionRange range = VersionRangeProcessor.createFromString( versionRangeDirect);
			Assert.assertTrue( "[" + VersionRangeProcessor.toString(range) + "] doesn't match [" + versionAsLowerBounds+ "]", VersionRangeProcessor.matches( range, versionAsLowerBounds));
			
			range = VersionRangeProcessor.createFromString( versionRangeOpen);
			Assert.assertTrue( "[" + VersionRangeProcessor.toString(range) + "]  matches [" + versionAsTooLow + "]", !VersionRangeProcessor.matches( range, versionAsTooLow));
		} catch (VersionProcessingException e) {
			fail("Exception thrown " + e);
		}
	}

	@Test
	public void testBestMatches() {
		try {
			List<Version> versions = createSortedListOfVersions();
			VersionRange versionRange = VersionRangeProcessor.createfromVersion( versionUpperBounds);
			Version version = VersionRangeProcessor.bestMatches(versionRange, versions);
			Assert.assertEquals( versionUpperBounds, version);
		} catch (VersionProcessingException e) {
			fail("Exception thrown " + e);
		}
	}

	@Test
	public void testHardMatchesVersionRangeListOfVersion() {
		try {
			List<Version> versions = createSortedListOfVersions();
			VersionRange versionRange = VersionRangeProcessor.createfromVersion( versionUpperBounds);
			Version version = VersionRangeProcessor.bestMatches(versionRange, versions);
			Assert.assertEquals( versionUpperBounds, version);
		} catch (VersionProcessingException e) {
			fail("Exception thrown " + e);
		}
	}

	@Test
	public void testIsFuzzy() {
		try {
			VersionRange range = VersionRangeProcessor.createFromString( versionRangeDirect);
			Assert.assertTrue( "Range is fuzzy", !VersionRangeProcessor.isFuzzy(range));
			
			range = VersionRangeProcessor.createFromString( versionRangeClosed);
			Assert.assertTrue( "Range is not fuzzy", !VersionRangeProcessor.isFuzzy(range));
			
		} catch (VersionProcessingException e) {
			fail( "Exception thrown");
		}
	}

	@Test
	public void testGetBestGuess() {
		Assert.assertTrue( true);
	}

	
	private List<Version> createSortedListOfVersions() {
		List<Version> result = new ArrayList<Version>();
		result.add( versionLowerBounds);
		result.add( versionUpperBounds);
		result.add( versionTooHigh);
		result.add( versionTooLow);
		
		Collections.sort( result, VersionProcessor::compare);
		return result;
	}
	
	@Test
	public void testIsHigher() {
		try {
			VersionRange range1 = VersionRangeProcessor.createFromString( versionAsLowerBounds);
			VersionRange range2 = VersionRangeProcessor.createFromString( versionAsUpperBounds);
			VersionRange range3 = VersionRangeProcessor.createFromString( versionAsTooHigh);
			VersionRange range4 = VersionRangeProcessor.createFromString( versionAsTooLow);
			
			Assert.assertEquals(versionAsLowerBounds + " is not higher than " + versionAsUpperBounds,VersionRangeProcessor.isHigher( range1, range2), false); 
			Assert.assertEquals(versionAsLowerBounds + " is not higher than " + versionAsTooHigh,VersionRangeProcessor.isHigher( range1, range3), false);
			Assert.assertEquals(versionAsLowerBounds + " is higher than " + versionAsTooLow,VersionRangeProcessor.isHigher( range1, range4), true);
		} catch (VersionProcessingException e) {
			fail( "Exception's thrown " + e);
		}		
	}
	@Test
	public void testUndefinedRange() {
		boolean thrown = false;
		try {
			VersionRange undefinedRange = VersionRangeProcessor.createVersionRange();
			VersionRange range1 = VersionRangeProcessor.createFromString( versionAsLowerBounds);
			/*
			VersionRange range2 = VersionRangeProcessor.createFromString( versionAsUpperBounds);
			VersionRange range3 = VersionRangeProcessor.createFromString( versionAsTooHigh);
			VersionRange range4 = VersionRangeProcessor.createFromString( versionAsTooLow);
			*/
			Assert.assertEquals(versionAsLowerBounds + " is not higher than undefined",VersionRangeProcessor.isHigher( range1, undefinedRange),true); 
			//Assert.assertEquals(versionAsLowerBounds + " is  not less than undefined", VersionRangeProcessor.isLess( range1, undefinedRange), true);
			
		} catch (VersionProcessingException e) {
			thrown = true;
		}
		Assert.assertTrue("Expected exception not thrown", thrown);
	}
	
	@Test
	public void testExplicitExclusion() {
		try {
			Version exclusionTestVersion0 = VersionProcessor.createFromString("3.9.9");
			Version exclusionTestVersion1 = VersionProcessor.createFromString("4.0");
			Version exclusionTestVersion2 = VersionProcessor.createFromString( "4.0.1");
			Version exclusionTestVersion3 = VersionProcessor.createFromString( "4.1");
			VersionRange exclusionTestRange = VersionRangeProcessor.createFromString("4.0.^");
			
			boolean val0 = VersionRangeProcessor.matches(exclusionTestRange, exclusionTestVersion0);
			boolean val1 = VersionRangeProcessor.matches(exclusionTestRange, exclusionTestVersion1);
			boolean val2 = VersionRangeProcessor.matches(exclusionTestRange, exclusionTestVersion2);
			boolean val3 = VersionRangeProcessor.matches(exclusionTestRange, exclusionTestVersion3);
			
			Assert.assertTrue( "3.9.9 is included in range 4.0.^", !val0);
			Assert.assertTrue( "4.0 is not included in range 4.0.^", val1);
			Assert.assertTrue( "4.0.1 is not included in range 4.0.^", val2);
			Assert.assertTrue( "4.1 is included in range 4.0.^", !val3);
		} catch (VersionProcessingException e) {

			fail( "Exception's thrown " + e);
		}
		
	}
	
	@Test
	public void testBeanShellVersion() {
		
		try {
			Version version1 = VersionProcessor.createFromString( beanShellRange1);
			Version version2 = VersionProcessor.createFromString( beanShellRange2);
			
			boolean isHigher = VersionProcessor.isHigher(version2, version1);
			boolean isLess = VersionProcessor.isLess(version1, version2);
		Assert.assertTrue( "[" + version2 + "] is NOT higher as [" + version1 + "]", isHigher);
		Assert.assertTrue( "[" + version1 + "] is NOT less as [" + version2 + "]", isLess);
			
		} catch (VersionProcessingException e) {
			fail( "Exception's thrown " + e);
		}
		
	}
	
	@Test
	public void testBeanShellRange() {
		
		try {
			VersionRange version1 = VersionRangeProcessor.createFromString( beanShellRange1);
			VersionRange version2 = VersionRangeProcessor.createFromString( beanShellRange2);
			
			boolean isHigher = VersionRangeProcessor.isHigher(version2, version1);
			boolean isNotHigher = VersionRangeProcessor.isHigher(version1, version2);
			boolean isLess = VersionRangeProcessor.isLess(version1, version2);
			boolean isNotLess = VersionRangeProcessor.isLess(version2, version1);
		Assert.assertTrue( "[" + version2.getOriginalVersionRange() + "] is NOT higher as [" + version1.getOriginalVersionRange() + "]", isHigher);
		Assert.assertTrue( "[" + version1.getOriginalVersionRange() + "] is higher as [" + version2.getOriginalVersionRange() + "]", !isNotHigher);
		Assert.assertTrue( "[" + version1.getOriginalVersionRange() + "] is NOT less as [" + version2.getOriginalVersionRange() + "]", isLess);
		Assert.assertTrue( "[" + version2.getOriginalVersionRange() + "] is less as [" + version1.getOriginalVersionRange() + "]", !isNotLess);
		} catch (VersionProcessingException e) {
			fail( "Exception's thrown " + e);
		}
		
	}
	
	@Test
	public void testRangeMetrics() {
		// test higher
		try {
			VersionRange range1 = VersionRangeProcessor.createFromString(rangeMetricsTestRangeUpperOpen);		
			VersionRange range2 = VersionRangeProcessor.createFromString(rangeMetricsTestRange2);
			
			Assert.assertTrue( "[" + rangeMetricsTestRangeUpperOpen + "] is not higher as [" + rangeMetricsTestRange2 + "]", VersionRangeProcessor.isHigher(range1, range2));
			Assert.assertTrue( "[" + rangeMetricsTestRange2 + "] is higher as [" + rangeMetricsTestRangeUpperOpen + "]", !VersionRangeProcessor.isHigher(range2, range1));
		} catch (VersionProcessingException e) {
			fail( "Exception's thrown " + e);
		}
		
		
		// test lower
		try {
			VersionRange range1 = VersionRangeProcessor.createFromString(rangeMetricsTestRangeUpperClosed);		
			VersionRange range2 = VersionRangeProcessor.createFromString(rangeMetricsTestRange2);
			
			Assert.assertTrue( "[" + rangeMetricsTestRange2 + "] is less as [" + rangeMetricsTestRangeUpperClosed + "]", !VersionRangeProcessor.isLess(range2, range1));
			Assert.assertTrue( "[" + rangeMetricsTestRangeUpperClosed + "] is less as [" + rangeMetricsTestRange2 + "]", !VersionRangeProcessor.isLess(range1, range2));
		} catch (VersionProcessingException e) {
			fail( "Exception's thrown " + e);
		}
	}
	
	/**
	 * test for closed / open ranges and single versions on the boundaries
	 */
	@Test
	public void testBorderRangeMetrics() {
		try {
			// upper edge : single version 2.0
			VersionRange range1UpperOpen = VersionRangeProcessor.createFromString(rangeMetricsTestRangeUpperOpen);
			VersionRange range1UpperClosed = VersionRangeProcessor.createFromString( rangeMetricsTestRangeUpperClosed);
			VersionRange range3 = VersionRangeProcessor.createFromString( rangeMetricsTestRange3);
			
			Assert.assertTrue("[" + rangeMetricsTestRange3 + "] is not higher as [" + rangeMetricsTestRangeUpperClosed + "]", VersionRangeProcessor.isHigher(range3, range1UpperClosed));
			Assert.assertTrue("[" + rangeMetricsTestRangeUpperClosed + "] is not less as [" + rangeMetricsTestRange3 + "]", !VersionRangeProcessor.isHigher(range1UpperClosed, range3));

			Assert.assertTrue("[" + rangeMetricsTestRangeUpperOpen + "] is higher as [" + rangeMetricsTestRange3 + "]", !VersionRangeProcessor.isHigher(range3, range1UpperOpen));
			//Assert.assertTrue("[" + rangeMetricsTestRange3 + "] is not less as [" + rangeMetricsTestRangeUpperOpen + "]", VersionRangeProcessor.isHigher(range1UpperOpen, range3));
			
			// lower edge : single version 1.0
			VersionRange range1LowerOpen = VersionRangeProcessor.createFromString(rangeMetricsTestRangeLowerOpen);
			VersionRange range1LowerClosed = VersionRangeProcessor.createFromString( rangeMetricsTestRangeLowerClosed);
			VersionRange range2 = VersionRangeProcessor.createFromString( rangeMetricsTestRange2);
			
			Assert.assertTrue("[" + rangeMetricsTestRange2 + "] is not less as [" + rangeMetricsTestRangeLowerClosed + "]", VersionRangeProcessor.isLess(range2, range1LowerClosed));
			//Assert.assertTrue("[" + rangeMetricsTestRangeLowerClosed + "] is not higher as [" + rangeMetricsTestRange2 + "]", !VersionRangeProcessor.isLess(range1LowerClosed, range2));

			Assert.assertTrue("[" + rangeMetricsTestRangeLowerOpen + "] is less as [" + rangeMetricsTestRange2 + "]", !VersionRangeProcessor.isLess(range2, range1LowerOpen));
			//Assert.assertTrue("[" + rangeMetricsTestRange2 + "] is not higher as [" + rangeMetricsTestRangeLowerOpen + "]", VersionRangeProcessor.isLess(range1LowerOpen, range2));
			
			
			// undefined edges : lower test 1.0, upper test 2.0
			VersionRange rangeLowerUndefined = VersionRangeProcessor.createFromString( rangeMetricsTestRangeLowerUndefined);
			Assert.assertTrue("[" + rangeMetricsTestRangeLowerUndefined + "] is not less as [" +rangeMetricsTestRange2 + "]", VersionRangeProcessor.isLess(rangeLowerUndefined, range2));
			Assert.assertTrue("[" + rangeMetricsTestRange2 + "] is less as [" +rangeMetricsTestRangeLowerUndefined + "]", !VersionRangeProcessor.isLess( range2, rangeLowerUndefined));
			
			VersionRange rangeUpperUndefined = VersionRangeProcessor.createFromString( rangeMetricsTestRangeUpperUndefined);
			Assert.assertTrue("[" + rangeMetricsTestRangeUpperUndefined + "] is not higher as [" + rangeMetricsTestRange3 + "]", VersionRangeProcessor.isHigher(rangeUpperUndefined, range3));
			Assert.assertTrue("[" + rangeMetricsTestRange3 + "] is higher as [" + rangeMetricsTestRangeUpperUndefined + "]", !VersionRangeProcessor.isHigher(range3, rangeUpperUndefined));
			
		} catch (VersionProcessingException e) {
			fail( "Exception's thrown " + e);

		}				
	}
	
	@Test
	public void testBorderRangeMetricsOnHotfix() {
		VersionRange range1 = VersionRangeProcessor.createFromString( "[1.0,1.1)");
		VersionRange range2 = VersionRangeProcessor.createFromString( "(1.0,1.1]");
		
		Version version1 = VersionProcessor.createFromString( "1.0.0");
		Version version2 = VersionProcessor.createFromString( "1.0.1");
		Version version3 = VersionProcessor.createFromString( "1.1.0");
		
		
		Assert.assertTrue( "1.0.0 doesn't fit into [1.0,1.1)", VersionRangeProcessor.matches(range1, version1));
		Assert.assertTrue( "1.0.0 doesn't fit into (1.0,1.1]", VersionRangeProcessor.matches(range2, version2));
		Assert.assertTrue( "1.1.0 doesn't fit into (1.0,1.1]", VersionRangeProcessor.matches(range2, version3));
		Assert.assertTrue( "1.1.0 fits into [1.0,1.1)", !VersionRangeProcessor.matches(range1, version3));
	}
	
	@Test
	public void testComparison() {
		VersionRange range1 = VersionRangeProcessor.createFromString( "[1.0,1.1)");
		VersionRange range2 = VersionRangeProcessor.createFromString( "[1.0,1.1)");		
		VersionRange range3 = VersionRangeProcessor.createFromString( "[1.1,1.2)");
		
		int v = VersionRangeProcessor.compare(range1, range2);
		Assert.assertTrue( "0 expected, found [" + v + "]", v == 0);
		
		v = VersionRangeProcessor.compare(range1, range1);
		Assert.assertTrue( "0 expected, found [" + v + "]", v == 0);
		
		v = VersionRangeProcessor.compare(range1, range3);
		Assert.assertTrue( "-1 expected, found [" + v + "]", v == -1);
		
		v = VersionRangeProcessor.compare(range3, range1);
		Assert.assertTrue( "1 expected, found [" + v + "]", v == 1);
		
		VersionRange collapsedRange1 = VersionRangeProcessor.createFromString( "1.1");
		VersionRange collapsedRange2 = VersionRangeProcessor.createFromString( "1.0");
		VersionRange collapsedRange3 = VersionRangeProcessor.createFromString( "1.2");
		
		v = VersionRangeProcessor.compare(range1, collapsedRange1);
		Assert.assertTrue( "-1 expected, found [" + v + "]", v == -1);
		
		v = VersionRangeProcessor.compare(range1, collapsedRange2);
		Assert.assertTrue( "1 expected, found [" + v + "]", v == 1);
		
		v = VersionRangeProcessor.compare(range3, collapsedRange2);
		Assert.assertTrue( "1 expected, found [" + v + "]", v == 1);
	}
}
