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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.artifacts.test.maven.pom.marshall.validator.BasicValidatorContext;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;

/**
 * test the handling of processing instructions, i.e. direct group assignement, calling of the manipulation parser and virtual part creation 
 * 
 * @author pit
 *
 */
public class ProcessingInstructionTest extends AbstractPomMarshallerTest {
	
	
	@Override
	public boolean validate(Solution solution) {
		String groupId = "com.braintribe.test";
		if (!validateHeader(solution, new BasicValidatorContext( groupId, "ProcessingInstructions", "1.0"))) {
			Assert.fail( "header not as expected");
			return false;
		}
		
		List<Dependency> dependencies = solution.getDependencies();
		
		// direct group processing 
		Dependency dep1 = retrieveDependency(dependencies, groupId, "DependencyWithGroup", "1.0");
		if (dep1 == null) {
			Assert.fail("dependency com.braintribe.test:DependencyWithGroup#1.0 not found");
			return false;
		}
		BasicValidatorContext c1 = new BasicValidatorContext( dep1, groupId, "DependencyWithGroup", "1.0");
		c1.setGroup( "DependencyGroup1");
		validateDependency(c1);
		
		
		// virtual part
		Dependency dep2 = retrieveDependency(dependencies, groupId, "DependencyWithVirtualPart", "1.0");
		if (dep2 == null) {
			Assert.fail("dependency com.braintribe.test:DependencyWithVirtualPart#1.0 not found");
			return false;
		}
		BasicValidatorContext c2 = new BasicValidatorContext( dep2, groupId, "DependencyWithVirtualPart", "1.0");
		Map<String,String> vp = new HashMap<>();
		vp.put( "asset:man", "$natureType = com.braintribe.model.asset.natures.CustomCartridge");
		c2.setVirtualParts(vp);
		validateDependency(c2);
		
		//validateVirtualPart(dep2, PartTupleProcessor.fromString("asset:man"), "$natureType = com.braintribe.model.asset.natures.CustomCartridge");
		
		// indirect group processing (via enrich)
		Dependency dep3 = retrieveDependency(dependencies, groupId, "DependencyWithManipulationParserGroup", "1.0");
		if (dep3 == null) {
			Assert.fail("dependency com.braintribe.test:DependencyWithManipulationParserGroup#1.0 not found");
			return false;
		}
		BasicValidatorContext c3 = new BasicValidatorContext( dep3, groupId, "DependencyWithManipulationParserGroup", "1.0");
		c3.setGroup( "DependencyGroup2");
		validateDependency(c3);
						
		
		return true;
	}

	@Test
	public void dependenciesTest() {
		read( "processingInstructions.xml");
	}

}
