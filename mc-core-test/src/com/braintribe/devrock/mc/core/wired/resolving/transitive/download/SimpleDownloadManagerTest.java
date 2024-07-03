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
