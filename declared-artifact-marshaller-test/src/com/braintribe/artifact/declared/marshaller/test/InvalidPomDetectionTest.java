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
package com.braintribe.artifact.declared.marshaller.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.artifact.declared.marshaller.DeclaredArtifactMarshaller;
import com.braintribe.model.artifact.declared.DeclaredArtifact;

/**
 * tests different poms with duplicate entries - all well-formed though
 * @author pit
 *
 */
public class InvalidPomDetectionTest {
	protected File input = new File( "res/input");
	protected DeclaredArtifactMarshaller marshaller = new DeclaredArtifactMarshaller();
	
	
	protected DeclaredArtifact read( String fileName) throws Exception {
		File file = new File( input, fileName);
		try ( InputStream in = new FileInputStream(file)) {
			return (DeclaredArtifact) marshaller.unmarshall( in);
		} 	
		catch (IOException e) {
			Assert.fail( "cannot read [" + fileName + "]");
			return null;
		}
		
	}
	
	protected void runTestOnExpectedInvalidPom(String fileName) {
		boolean exceptionThrown = false;
		try {
			read( fileName);
		} catch (Exception e) {
			exceptionThrown = true;
		}
		
		Assert.assertTrue("unexpectedly [" + fileName + "] was a valid pom file", exceptionThrown);
	}
 
	
	@Test
	public void runBaseLineTests() {
		String fileName = "base.line.pom.xml";
		try {
			read( fileName);
		} catch (Exception e) {
			Assert.fail("unexpectedly [" + fileName + "] was not a valid pom file");
		}
	}
	
	// single properties 
	
	// multiple groupId
	@Test
	public void runTestWithGroupIds() {
		runTestOnExpectedInvalidPom("groupId.invalid.pom.xml");
	}
	// multiple artifactId
	@Test
	public void runTestWithArtifactIds() {
		runTestOnExpectedInvalidPom("artifactId.invalid.pom.xml");
	}		

	// multiple version
	@Test
	public void runTestWithVersions() {
		runTestOnExpectedInvalidPom("version.invalid.pom.xml");
	}		
	// multiple packaging
	@Test
	public void runTestWithPackagings() {
		runTestOnExpectedInvalidPom("packaging.invalid.pom.xml");
	}	
	
	// collections 
	
	// multiple dependencies
	@Test
	public void runTestWithMultipleDependencies() {
		runTestOnExpectedInvalidPom("dependencies.invalid.pom.xml");
	}
	// multiple dependency management
	@Test
	public void runTestWithMultipleDependencyManagement() {
		runTestOnExpectedInvalidPom("dependency.management.invalid.pom.xml");
	}
	
	// multiple distribution management
	@Test
	public void runTestWithMultipleDistributionManagement() {
		runTestOnExpectedInvalidPom("distribution.management.invalid.pom.xml");
	}
	// multiple licenses
	@Test
	public void runTestWithMultipleLicenses() {
		runTestOnExpectedInvalidPom("licenses.invalid.pom.xml");
	}	
	// multiple properties 
	@Test
	public void runTestWithMultipleProperties() {
		runTestOnExpectedInvalidPom("properties.invalid.pom.xml");
	}			
	
	
	
	
}
