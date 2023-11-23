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
package com.braintribe.artifacts.codebase.walk;

import java.io.File;

import org.junit.BeforeClass;

public class StdFlatCodebaseAwareWalkLab extends AbstractCodebaseAwareWalkLab {
	
	@BeforeClass
	public static void runBefore() {
		
		target = new File( contents, "standard.flattened");
		targetGrpOne = new File( target, "com.braintribe.grpOne/A/1.0/pom.xml");
		targetTerminal = new File( target, "com.braintribe.terminal/Terminal/1.0/pom.xml");		
		setupCodebase( FLATTENED_STANDARD);
		runbefore( FLATTENED_STANDARD);
	}

}
