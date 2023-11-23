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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.partreflection;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.model.artifact.consumable.PartReflection;

/**
 * tests reflection on an remote repository with RestSupport only 
 * @author pit
 *
 */
public class SimplePartReflectionTest extends AbstractPartReflectionTest {
	public SimplePartReflectionTest() {	
			launcher = Launcher.build()
					.repolet()
					.name("archive")
						.descriptiveContent()
							.descriptiveContent( archiveInput( "simple.definition.txt"))
						.close()
						.restApiUrl("http://localhost:${port}/api/storage/archive")
						.serverIdentification("Artifactory/faked.by.repolet")			
					.close()
					.done();	
	}

	
	@Test
	public void simplePartReflectionOnTerminalTest() {
		
		List<PartReflection> expectations = new ArrayList<>();
		
		String repoletRoot = launcher.getLaunchedRepolets().get("archive");
		expectations.add( PartReflection.create( null, "pom", "archive"));
		expectations.add( PartReflection.create( null, "jar", "archive"));
		expectations.add( PartReflection.create( "sources", "jar", "archive"));
		expectations.add( PartReflection.create( "javadoc", "jar", "archive"));
		expectations.add( PartReflection.create( "asset", "man", "archive"));
					
		List<PartReflection> found = runResolveViaSettings("com.braintribe.devrock.test:t#1.0.1");		
		validate( found, expectations);
	}
	@Test
	public void simplePartReflectionOnB() {
		
		List<PartReflection> expectations = new ArrayList<>();
		
		String repoletRoot = launcher.getLaunchedRepolets().get("archive");
		expectations.add( PartReflection.create( null, "pom", "archive"));
		expectations.add( PartReflection.create( null, "jar", "archive"));
		expectations.add( PartReflection.create( "sources", "jar", "archive"));		
		
		List<PartReflection> found = runResolveViaSettings("com.braintribe.devrock.test.b:b#1.0.1");
		
		validate( found,  expectations);
	}
	@Test
	public void simplePartReflectionOnA() {
		
		
		List<PartReflection> expectations = new ArrayList<>();
		
		String repoletRoot = launcher.getLaunchedRepolets().get("archive");
		expectations.add( PartReflection.create( null, "pom", "archive"));
		expectations.add( PartReflection.create( null, "jar", "archive"));
		expectations.add( PartReflection.create( "sources", "jar", "archive"));
		expectations.add( PartReflection.create( "javadoc", "jar", "archive"));
		expectations.add( PartReflection.create( "asset", "man", "archive"));
		expectations.add( PartReflection.create( "properties", "zip", "archive"));

		
		List<PartReflection> found = runResolveViaSettings("com.braintribe.devrock.test.a:a#1.0.2");
		
		validate( found, expectations);
	}
}
