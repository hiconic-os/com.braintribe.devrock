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
