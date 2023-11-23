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

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.artifacts.test.maven.pom.marshall.validator.BasicValidatorContext;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;

/**
 * test dependencies 
 * 
 * @author pit
 *
 */
public class DependenciesTagTest extends AbstractPomMarshallerTest {
	
	
	@Override
	public boolean validate(Solution solution) {
		String groupId = "com.braintribe.test";
		if (!validateHeader(solution, new BasicValidatorContext(groupId, "A", "1.0"))) {
			Assert.fail( "header not as expected");
			return false;
		}
		
		List<Dependency> dependencies = solution.getDependencies();
		
		// 
		Dependency dep1 = retrieveDependency(dependencies, groupId, "Dep1", "1.0");
		if (dep1 == null) {
			Assert.fail("dependency com.braintribe.test:Dep1#1.0 not found");
			return false;
		}
		BasicValidatorContext c1 = new BasicValidatorContext(dep1, groupId, "Dep1", "1.0",  Arrays.asList("standard"));
		validateDependency(c1);
		
		Dependency dep2 = retrieveDependency(dependencies, groupId, "Dep2", "1.0");
		if (dep2 == null) {
			Assert.fail("dependency com.braintribe.test:Dep2#1.0 not found");
			return false;
		}
		BasicValidatorContext c2 = new BasicValidatorContext(dep2, groupId, "Dep2", "1.0",  Arrays.asList("one","two"));
		validateDependency(c2);		
		
		Dependency dep3 = retrieveDependency(dependencies, groupId, "Dep3", "1.0");
		if (dep3 == null) {
			Assert.fail("dependency com.braintribe.test:Dep3#1.0 not found");
			return false;
		}
		BasicValidatorContext c3 = new BasicValidatorContext(dep3, groupId, "Dep3", "1.0",  Arrays.asList());
		validateDependency(c3);				
		
		Dependency dep4 = retrieveDependency(dependencies, groupId, "Dep4", "1.0");
		if (dep4 == null) {
			Assert.fail("dependency com.braintribe.test:Dep4#1.0 not found");
			return false;
		}
		BasicValidatorContext c4 = new BasicValidatorContext(dep4, groupId, "Dep4", "1.0",  Arrays.asList("one","two","three"));
		validateDependency(c4);				
		
		return true;
	}

	@Test
	public void dependenciesTest() {
		read( "dependencyTags.xml");
	}

}
