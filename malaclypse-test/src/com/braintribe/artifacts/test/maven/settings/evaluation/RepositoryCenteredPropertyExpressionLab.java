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
