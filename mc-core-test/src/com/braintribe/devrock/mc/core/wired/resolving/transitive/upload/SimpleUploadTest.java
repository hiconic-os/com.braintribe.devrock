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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.upload;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.artifact.consumable.ArtifactResolution;
import com.braintribe.utils.paths.UniversalPath;

/**
 * uploads a single artifact to the repolet 
 * @author pit
 *
 */
public class SimpleUploadTest extends AbstractUploadTest {

	private static final String TEST_ARTIFACT = "com.braintribe.devrock.test:t#1.0.2";

	@Override
	protected RepoletContent archiveInput() {	
		return RepoletContent.T.create();
	}

	private void run(String repositoryId, File targetRoot, boolean expectHashes) {
		File directory = new File( input, "t-1.0.2");
		ArtifactResolution resolution = runSingle( TEST_ARTIFACT, directory, repositoryId);
		
		if (resolution.hasFailed()) {
			Assert.fail("upload failed : " + resolution.getFailure().stringify());
		}
		
		// validate expectations
		Artifact source = generateArtifact(TEST_ARTIFACT, directory);
								
		File targetDirectory = UniversalPath.from(targetRoot).push("com.braintribe.devrock.test.t", ".").push("1.0.2").toFile();		
		Artifact target = generateArtifact(TEST_ARTIFACT, targetDirectory);
		
		Validator validator = new Validator();		
		validator.validate( source, target, expectHashes);		
		validator.assertResults();
	}

	@Test
	public void runSingleUploadTest() {		
		run( "archive", upload, true);				
	}
	@Test
	public void runSingleUploadToFsTest() {
		run( "fs-archive", fsRepo, false);
	}

}
