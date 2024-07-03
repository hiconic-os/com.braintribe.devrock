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

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.artifacts.test.maven.pom.marshall.validator.BasicValidatorContext;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;

/**
 * test parent reference 
 * @author pit
 *
 */
public class ParentReferenceTest extends AbstractPomMarshallerTest {
	
	
	@Override
	public boolean validate(Solution solution) {
		if (!validateHeader(solution, new BasicValidatorContext("com.braintribe.test", "A", "1.0"))) {
			Assert.fail( "header not as expected");
			return false;
		}
		Dependency parent = solution.getParent();
		if (parent == null) {
			Assert.fail("no parent found");
			return false;
		}
		if (!validateDependency(new BasicValidatorContext(parent, "com.braintribe.test", "Parent", "1.0"))) {
			Assert.fail( "parent not as expected");
			return false;
		}
		
		return true;
	}

	@Test
	public void parentReferenceTest() {
		read( "parentRef.xml");
	}

}
