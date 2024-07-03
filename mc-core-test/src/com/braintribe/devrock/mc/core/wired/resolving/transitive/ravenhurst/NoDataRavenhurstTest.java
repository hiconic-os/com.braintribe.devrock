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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.ravenhurst;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.repolet.launcher.Launcher;

/**
 * simple test that tests whether RH processing has no issues with a non-existing last-changes-access file 
 * @author pit
 *
 */
public class NoDataRavenhurstTest extends AbstractRavenhurstTest {
	
	@Override
	protected Launcher launcher() {			 
		Launcher launcher = Launcher.build()
				.repolet()
				.name("archive")					
					.changesUrl("http://localhost:${port}/archive/rest/changes")
					.descriptiveContent()
						.descriptiveContent( archiveInput(new File( input, "archive.definition.stage.1.yaml")))
					.close()
//					
//					.indexedDescriptiveContent()
//						.initialIndex("stage1")
//						.descriptiveContent("stage1", archiveInput( new File( input, "archive.definition.stage.1.yaml")))
//						.descriptiveContent("stage2", archiveInput( new File( input, "archive.definition.stage.2.yaml")))
//					.close()
				.close()
			.done();		
		return launcher;
	}
	

	@Test
	public void runTest() {
		try {		
			run( "com.braintribe.devrock.test:t#1.0.1", standardTransitiveResolutionContext, true);
		} catch (Exception e) {
			Assert.fail("exception thrown :" + e.getMessage());
		}
	}

}
