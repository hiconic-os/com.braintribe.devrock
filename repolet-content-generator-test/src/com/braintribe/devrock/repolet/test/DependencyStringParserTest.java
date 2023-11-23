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
package com.braintribe.devrock.repolet.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.model.repolet.content.Dependency;


public class DependencyStringParserTest {

	private Map<String, Dependency> testCases = new HashMap<>();
	{
		Dependency dp = from( "group", "artifact", "1.0");		
		testCases.put("group:artifact#1.0", dp);
		
		dp = from( "group", "artifact", "1.0");
		dp.setClassifier("classifier");
		dp.setScope( "scope");
		dp.setType("type");	
		testCases.put("group:artifact#1.0-classifier:scope:type", dp);
		
		dp = from( "group", "artifact", "1.0");
		dp.setClassifier("classifier");		
		dp.setType("type");
		testCases.put("group:artifact#1.0-classifier:type", dp);
		
		dp = from( "group", "artifact", "1.0");
		dp.setClassifier("classifier");
		dp.setScope( "scope");		
		testCases.put("group:artifact#1.0-classifier:scope:", dp);
		
		
		
	}
	
	

	private Dependency from(String grp, String art, String vrs) {
		Dependency d = Dependency.T.create();
		d.setGroupId(grp);
		d.setArtifactId(art);
		d.setVersion(vrs);
		return d;
	}
	
	private void validate(String tag, String expected, String found) {
		if (expected == null) {			
			Assert.assertTrue("expected [null], but found [" + found + "]", found == null);							
		}
		else {
			if (found == null) {
				Assert.assertTrue("expected [" + expected + "], but found [" + found + "]", found != null);
			}
			else {
				Assert.assertTrue("expected [" + expected + "], but found [" + found + "]", found.equals(expected));
			}
		}
		
	}

	private void validate(Dependency expected, Dependency found) {
		validate("group", expected.getGroupId(), found.getGroupId());
		validate( "artifact", expected.getArtifactId(), found.getArtifactId());
		validate("version", expected.getVersion(), expected.getVersion());
		validate("classifier", expected.getClassifier(), expected.getClassifier());
		validate("scope", expected.getScope(), expected.getScope());
		validate("type", expected.getType(), expected.getType());		
	}
	
	@Test
	public void test() {
		for (Map.Entry<String, Dependency> entry : testCases.entrySet()) {
			Dependency found = Dependency.parse( entry.getKey());
			validate( entry.getValue(), found);
		}
	}
	
	//@Test
	public void relocationExpressionTest() {
		String partial1 = "com.braintribe.test.redirect";
		String partial2 = "com.braintribe.test.redirect:a";
		String partial3 = ":a";
		String partial4 = ":a#1.0.1";
		String full = "com.braintribe.test.redirect:a#1.0.1";
		
		Dependency.parse( full, false);
		Dependency.parse( partial1, true);
		Dependency.parse( partial2, true);
		Dependency.parse( partial3, true);
		Dependency.parse( partial4, true);
		
	}
}
