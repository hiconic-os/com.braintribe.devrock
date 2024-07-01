// ============================================================================
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
package com.braintribe.devrock.mc.core.identifications;



import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.version.FuzzyVersion;
import com.braintribe.model.version.VersionExpression;

/**
 * all tests for diverse 'identifications', such as {@link CompiledDependencyIdentification} etc 
 * @author pit
 *
 */
public class IdentificationTest {

	@Test
	public void rangifyingCompiledDependenciesTest() {
		String standard = "com.braintribe.devrock.test:artifact#1.0";
		
		CompiledDependencyIdentification standardCDI = CompiledDependencyIdentification.parseAndRangify(standard);
		VersionExpression standardExpression = standardCDI.getVersion();
		Assert.assertTrue("expected expression to be a range, but it's a [" + standardExpression.getClass().getName() + "]", standardExpression instanceof FuzzyVersion);
	
		FuzzyVersion fuzzy = (FuzzyVersion) standardExpression;
		
		String lowerBoundsAsString = fuzzy.lowerBound().asString();
		Assert.assertTrue( "expected lower bounds to be [1.0], but it's [" + lowerBoundsAsString + "]", lowerBoundsAsString.equals( "1.0"));		
		Assert.assertTrue( "expected lower bounds not to be exclusive, but it is", !fuzzy.lowerBoundExclusive());
		
		String upperBoundAsString = fuzzy.upperBound().asString();
		Assert.assertTrue( "expected upper bounds to be [1.1], but it's [" + upperBoundAsString + "]", upperBoundAsString.equals( "1.1"));		
		Assert.assertTrue( "expected upper bounds to be exclusive, but it is", fuzzy.upperBoundExclusive());
		
		// TODO : add tests for "transparent rangification" -> direct range in string expression etc
		//String ranged = "com.braintribe.devrock.test:artifact#[2.0, 3.1)";
		//CompiledDependencyIdentification rangedCDI = CompiledDependencyIdentification.parseAndRangify( ranged);
		
		
	}

}
