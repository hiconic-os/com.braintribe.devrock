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

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.artifact.processing.version.VersionMetricTuple;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.version.Version;

public class VersionMetricTupleTests {
	
	private String v1 = "1";
	private String v2 = "1.0";
	private String v3 = "1.0.0";
	private String v4 = "1.0.0.0";
	private String v3a = "1.0.0.GA";
	private String v3b = "1.0.0GA";
	private String vpc = "1.0.1-pc";
	private String vpc2 = "1.0.1pc";
	
	private String formatMetricTuple( VersionMetricTuple tuple) {
		StringBuilder builder = new StringBuilder();
		Integer major = tuple.major;
		if (major != null)
			builder.append( major);
		
		Integer minor = tuple.minor;
		if (minor != null) {
			builder.append(".");
			builder.append( minor);
		}
		Integer revision = tuple.revision;
		if (revision != null) {
			builder.append(".");
			builder.append( revision);
		}		
		return builder.toString();
	}

	@Test
	public void testExtractionSequence() {

		try {
			Version version = VersionProcessor.createFromString(v1);
			VersionMetricTuple tuple = VersionProcessor.getVersionMetric(version);
			System.out.println( v1 + "->" + formatMetricTuple(tuple));
			
			version = VersionProcessor.createFromString(v2);
			tuple = VersionProcessor.getVersionMetric(version);
			System.out.println( v2 + "->" + formatMetricTuple(tuple));
			
			version = VersionProcessor.createFromString(v3);
			tuple = VersionProcessor.getVersionMetric(version);
			System.out.println( v3 + "->" + formatMetricTuple(tuple));
			
			version = VersionProcessor.createFromString(v4);
			tuple = VersionProcessor.getVersionMetric(version);
			System.out.println( v4 + "->" + formatMetricTuple(tuple));
			
			
		} catch (VersionProcessingException e) {
			e.printStackTrace();
			Assert.fail( "Exception [" + e + "] thrown");
		}		
	}
	
	@Test
	public void testWeirdExtractionSequence() {
		try {
			Version version = VersionProcessor.createFromString(v3a);
			VersionMetricTuple tuple = VersionProcessor.getVersionMetric(version);
			System.out.println( v3a + "->" + formatMetricTuple(tuple));
			
			version = VersionProcessor.createFromString(v3b);
			tuple = VersionProcessor.getVersionMetric(version);
			System.out.println( v3b + "->" + formatMetricTuple(tuple));
			
			
		} catch (VersionProcessingException e) {
			e.printStackTrace();
			Assert.fail( "Exception [" + e + "] thrown");
		}		
	}
	
	 
	private void testSettingSequence( String v) {
		try {
			Version bversion = VersionProcessor.createFromString(v);
			VersionMetricTuple btuple = VersionProcessor.getVersionMetric( bversion);
			VersionMetricTuple atuple = new VersionMetricTuple();
			atuple.major = btuple.major + 1;
			atuple.minor = btuple.minor + 1;
			atuple.revision = btuple.revision + 1;
			VersionProcessor.setVersionMetric(bversion, atuple);			
			System.out.println( formatMetricTuple(btuple) + "->" + VersionProcessor.toString(bversion));
			
			
		} catch (VersionProcessingException e) {
			e.printStackTrace();
			Assert.fail( "Exception [" + e + "] thrown");
		}
	}
	
	@Test 
	public void testSettingSequences() {
		testSettingSequence( v1);
		testSettingSequence( v2);
		testSettingSequence( v3);		
	}
	
	@Test
	public void testWeirdSettingSequences() {
		testSettingSequence( v3a);	
		testSettingSequence( v3b);	
	}
	
	@Test
	public void testVersionMetricsAssignment() {
		VersionMetricTuple tuple = new VersionMetricTuple();
		tuple.major = 1;
		tuple.minor = 0;
		
		Version version = VersionProcessor.createFromMetrics(tuple);
		
		System.out.println( VersionProcessor.toString(version));
	}
	
	@Test
	public void testVersionMetricsOnPc() {
		Version version = VersionProcessor.createFromString(vpc);
		VersionMetricTuple tuple = VersionProcessor.getVersionMetric(version);
		System.out.println( vpc + "->" + formatMetricTuple(tuple));
		
		Assert.assertTrue( "expected value for [" + vpc + "] is 1.0.1, yet [" + formatMetricTuple(tuple) + "] encountered", formatMetricTuple(tuple).equalsIgnoreCase("1.0.1"));
	}

	@Test
	public void testVersionMetricsOnPc2() {
		Version version = VersionProcessor.createFromString(vpc2);
		VersionMetricTuple tuple = VersionProcessor.getVersionMetric(version);
		System.out.println( vpc2 + "->" + formatMetricTuple(tuple));
		Assert.assertTrue( "expected value for [" + vpc2 + "] is 1.0.0, yet [" + formatMetricTuple(tuple) + "] encountered", formatMetricTuple(tuple).equalsIgnoreCase("1.0.0"));
	}
}
