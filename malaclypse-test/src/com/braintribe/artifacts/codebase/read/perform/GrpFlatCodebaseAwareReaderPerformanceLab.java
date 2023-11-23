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
package com.braintribe.artifacts.codebase.read.perform;

import java.io.File;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import com.braintribe.model.artifact.Solution;
import com.braintribe.testing.category.KnownIssue;

@Category(KnownIssue.class)
public class GrpFlatCodebaseAwareReaderPerformanceLab extends AbstractCodebaseAwareReaderPerformanceLab {

	@BeforeClass
	public static void runBefore() {		
		masterCodebase = new File( System.getenv( "BT__ARTIFACTS_HOME"), "../artifact-groups" );		
		runbefore( FLATTENED_GROUP);
	}
	
	//@Test
	public void gaugeMalaclypseSingle() {
		Solution solution = singleRead( new File( masterCodebase, "com.braintribe.devrock/1.0/Malaclypse/pom.xml"));
		Assert.assertNotNull(solution);
	}

	//@Test
	public void gaugeMalaclypseMultiple() {
		Solution solution = multipleRead( new File( masterCodebase, "com.braintribe.devrock/1.0/Malaclypse/pom.xml"), 110, 10);		
		Assert.assertNotNull(solution);
	}
}
