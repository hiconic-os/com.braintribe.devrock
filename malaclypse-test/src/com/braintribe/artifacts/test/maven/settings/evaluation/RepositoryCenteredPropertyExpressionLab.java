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
package com.braintribe.artifacts.test.maven.settings.evaluation;



import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;



public class RepositoryCenteredPropertyExpressionLab {
	
	String [] ts = new String[] {"a,http://bla.com/bla", "b,https://bla.com/bla", "l,file://c:/local/bla", "lh,http://localhost/bla", "lr,http://bla.com/bla"};
	

	private class Tuple {
		String id;
		String url;
		boolean expectation;
		boolean result;
		
		public Tuple( String str, boolean b) {
			String [] s = str.split(",");
			id = s[0];
			url = s[1];
			expectation = b;
		}
	}
	
	private List<Tuple> extractTuples( String [] values, boolean [] expectations) {
		List<Tuple> result = new ArrayList<Tuple>();
		
		for (int i = 0; i < values.length; i++) {			
			Tuple tuple = new Tuple( values[i], expectations[i]);
			result.add(tuple);
		}
		return result;
	}
	 
	
	public void test(String [] tokens, String expression, boolean [] expectations) {
		for (Tuple tuple : extractTuples( tokens, expectations)) {
			boolean external;
			try {
				external = MavenSettingsReader.isExternalUrl( tuple.url);
			} catch (Exception e) {
				Assert.fail( e.getMessage());
				return;
			}
			tuple.result = MavenSettingsReader.isRepositoryNotedInPropertyValue( tuple.id, expression, external);
			Assert.assertTrue("result [" + tuple.result + "] of value [" + tuple.id + "@" + tuple.url + "] is not as expected [" + tuple.expectation + "]", tuple.result == tuple.expectation);
		}
		
	}
	
	@Test
	public void simpleTest() {
		String simpleExpression = "a,b,!l,l*";
		boolean [] simpleResults = {true, true, false, true, true};
		test( ts, simpleExpression, simpleResults);
	}
	@Test
	public void locationTest() {
		String externalExpression = "!external:a,external:b,!external:l*,l*";
		boolean [] externalResults = {false, true, true, true, false};
		test( ts, externalExpression, externalResults);
	}
}
