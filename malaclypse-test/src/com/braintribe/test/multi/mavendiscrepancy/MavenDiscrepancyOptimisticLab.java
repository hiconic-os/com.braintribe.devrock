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
package com.braintribe.test.multi.mavendiscrepancy;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import com.braintribe.test.multi.ClashStyle;
import com.braintribe.testing.category.KnownIssue;

/**
 * NOTE : A#1.0 is taken as its only reference is in B#1.0 which is taken over B#1.1, hence the request to 
 * A#1.1 is weeded. 
 * 
 * @author pit
 *
 */
@Category(KnownIssue.class)
public class MavenDiscrepancyOptimisticLab extends AbstractMavenDiscrepancyLab {	
	protected static File settings = new File( "res/mavenDiscrepancyLab/contents/settings.mavenDiscrepancy.xml");
	
	@BeforeClass
	public static void before() {
		before( settings);
	}

	//@Test 
	public void optimistic() {
		String [] optimistic = new String [] { "com.braintribe.test.dependencies.mavenDiscrepancyTest:A#1.0", 
												"com.braintribe.test.dependencies.mavenDiscrepancyTest:B#1.1",
												"com.braintribe.test.dependencies.mavenDiscrepancyTest:C#1.1",
		};
		runTest( optimistic, ClashStyle.optimistic);
	}
			
}
