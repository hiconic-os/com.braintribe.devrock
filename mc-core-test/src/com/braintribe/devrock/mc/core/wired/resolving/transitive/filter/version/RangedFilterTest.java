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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.filter.version;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

/**
 * tests the case with a version range in the filter.
 *  
 * @author pit
 *
 */
public class RangedFilterTest extends AbstractTransitiveResolverFilterTest {

	@Override
	protected File settings() {
		return new File( input, "settingsWithVersionFilter.xml");
	}

	
	@Test
	public void runVersionedFilterTest() {
		try {
			runTest( new File( input, "versionedFilter.validation.txt"));
		} catch (Exception e) {
			Assert.fail("exception thrown: " + e.getMessage());			
		}
	}
		

}
