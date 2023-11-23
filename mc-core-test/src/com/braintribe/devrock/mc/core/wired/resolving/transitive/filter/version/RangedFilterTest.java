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
