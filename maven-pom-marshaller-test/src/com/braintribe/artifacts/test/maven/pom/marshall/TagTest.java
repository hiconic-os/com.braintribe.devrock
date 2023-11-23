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
package com.braintribe.artifacts.test.maven.pom.marshall;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;

public class TagTest extends AbstractPomMarshallerTest {
	private static Map<String,List<String>> expected;
	
	@BeforeClass
	public static void before() {
		expected = new HashMap<>();
		expected.put("none", Arrays.asList("n/a"));
		expected.put("standard", Arrays.asList( "standard"));
		expected.put("one", Arrays.asList( "one"));
		expected.put("one-and-two", Arrays.asList( "one", "two"));
	}

	@Test
	public void test() {
		read( "tags-terminal-1.0.1.pom");
	}

	@Override
	public boolean validate(Solution solution) {
				
		
		List<Dependency> dependencies = solution.getDependencies();
		Assert.assertTrue( "expected [" + expected.size() + "] dependencies, found [" + dependencies.size() + "]", expected.size() == dependencies.size());
		
		for (Dependency dependency : dependencies) {
			List<String> foundTags = dependency.getTags();
			List<String> expectedTags = expected.get( dependency.getArtifactId());
			
			if (expectedTags.size() == 1 && expectedTags.get(0).equalsIgnoreCase("n/a")) {
				Assert.assertTrue( "no tags expected for [" + dependency.getArtifactId() + "], but found [" + foundTags.size()+ "]", foundTags.size() == 0);				
			}
			else {
				List<String> expected = new ArrayList<>( expectedTags);
				List<String> notFound = new ArrayList<>();
				Iterator<String> iterator = expected.iterator();
				while (iterator.hasNext()) {
					String exp = iterator.next();
					if (!foundTags.contains(exp)) {
						notFound.add(exp);
					}
				}
				Assert.assertTrue("expected values not found", notFound.size() == 0);
				Assert.assertTrue("found contains unexpected tags", expectedTags.size() == foundTags.size());
			}
			
		}
		
		return true;
	}
	
	

}
