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
