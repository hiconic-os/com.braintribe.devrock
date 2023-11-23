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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.download;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;

public class SimpleDownloadManagerTest extends AbstractDownloadManagerTest {

	public SimpleDownloadManagerTest() {
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
	public void test() {
		
		String availabilityTargetAsString = "com.braintribe.devrock.test:t#1.0.1";
		CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse( availabilityTargetAsString);
		
		Set<CompiledPartIdentification> expected = new HashSet<>(); 
		expected.add( CompiledPartIdentification.from( cai, PartIdentification.create( "pom")));
		expected.add( CompiledPartIdentification.from( cai, PartIdentification.create( "jar")));
		expected.add( CompiledPartIdentification.from( cai, PartIdentification.create("sources", "jar")));
		expected.add( CompiledPartIdentification.from( cai, PartIdentification.create("javadoc", "jar")));
		expected.add( CompiledPartIdentification.from( cai, PartIdentification.create("asset", "man")));
		 		
		run(availabilityTargetAsString, "archive");
		
		validate( repo, cai, expected);
	}
}
