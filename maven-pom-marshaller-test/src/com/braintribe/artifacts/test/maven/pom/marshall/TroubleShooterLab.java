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

import org.junit.Test;

import com.braintribe.model.artifact.Solution;

/**
 * test exclusions
 * 
 * @author pit
 *
 */
public class TroubleShooterLab extends AbstractPomMarshallerTest {
	
	
	@Override
	public boolean validate(Solution solution) {
	
		return true;
	
	
	}
	
	
	@Test
	public void testTCC() {
		for (int i = 0; i < 10000; i++) 
			read( "TribefireControlCenter-2.0.pom");
	}
	
	@Test
	public void junit412() {
		read( "junit-4.12.pom");
	}
	

}
