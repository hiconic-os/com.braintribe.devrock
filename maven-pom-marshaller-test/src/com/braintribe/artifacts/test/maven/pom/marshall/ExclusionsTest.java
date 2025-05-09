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
package com.braintribe.artifacts.test.maven.pom.marshall;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.artifacts.test.maven.pom.marshall.validator.BasicValidatorContext;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Exclusion;
import com.braintribe.model.artifact.Solution;

/**
 * test exclusions
 * 
 * @author pit
 *
 */
public class ExclusionsTest extends AbstractPomMarshallerTest {
	
	
	@Override
	public boolean validate(Solution solution) {
		String groupId = "com.braintribe.test";
		if (!validateHeader(solution, new BasicValidatorContext( groupId, "A", "1.0"))) {
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
		BasicValidatorContext c1 = new BasicValidatorContext(dep1, groupId, "Dep1", "1.0");
		validateDependency(c1);
				
		Exclusion emptyExclusion = Exclusion.T.create();
		validateExclusions(dep1, Collections.singleton( emptyExclusion));
		
		Dependency dep2 = retrieveDependency(dependencies, groupId, "Dep2", "1.0");
		if (dep2 == null) {
			Assert.fail("dependency com.braintribe.test:Dep2#1.0 not found");
			return false;
		}
		BasicValidatorContext c2 = new BasicValidatorContext(dep2, groupId, "Dep2", "1.0");
		validateDependency(c2);		
		
		Exclusion singleExclusion = Exclusion.T.create();
		singleExclusion.setGroupId("com.braintribe.exclusion");
		singleExclusion.setArtifactId( "Exclusion");
		validateExclusions(dep2, Collections.singleton( singleExclusion));
		
		Dependency dep3 = retrieveDependency(dependencies, groupId, "Dep3", "1.0");
		if (dep3 == null) {
			Assert.fail("dependency com.braintribe.test:Dep3#1.0 not found");
			return false;
		}
		BasicValidatorContext c3 = new BasicValidatorContext(dep3, groupId, "Dep3", "1.0");
		validateDependency(c3);		
		
		Exclusion exclusion1 = Exclusion.T.create();
		exclusion1.setGroupId("com.braintribe.exclusion");
		exclusion1.setArtifactId( "Exclusion1");
		
		Exclusion exclusion2 = Exclusion.T.create();
		exclusion2.setGroupId("com.braintribe.exclusion");
		exclusion2.setArtifactId( "Exclusion2");
		
		Set<Exclusion> exclusions = new HashSet<>();
		exclusions.add(exclusion1);
		exclusions.add(exclusion2);
		validateExclusions(dep3, exclusions);
		return true;
	}
	
	
	@Test
	public void redirectionTest() {
		read( "exclusions.xml");
	}

}
