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
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.artifact.consumable.ArtifactResolution;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.paths.UniversalPath;

/**
 * deploy multiple artifacts to the repolet
 * @author pit
 *
 */
@Category(KnownIssue.class)
public class MultipleUploadTest extends AbstractUploadTest {

	private static final String TEST_ARTIFACT_1 = "com.braintribe.devrock.test:t#1.0.2";
	private static final String TEST_ARTIFACT_2 = "com.braintribe.devrock.test:a#1.0.2";

	@Override
	protected RepoletContent archiveInput() {	
		return RepoletContent.T.create();
	}
	
	@Test
	public void runMultipleUploadTest() {
		run("archive", upload, true);			
	}
	
	@Test
	public void runMultipleUploadToFsTest() {
		run("fs-archive", fsRepo, false);			
	}

	private void run(String repositoryId, File targetRoot, boolean expectHashes) {
		Map<String,File> map = new HashMap<>();
		
		File tDirectory = new File( input, "t-1.0.2");
		map.put(TEST_ARTIFACT_1, tDirectory);
		
		File aDirectory = new File( input, "a-1.0.2");
		map.put(TEST_ARTIFACT_2, aDirectory);
		
		
		
		ArtifactResolution resolution = runMultiple( map, repositoryId);
		
		if (resolution.hasFailed()) {
			Assert.fail("upload failed : " + resolution.getFailure().stringify());
		}
		
		Validator validator = new Validator();		

		
		// validate expectations
		Artifact source1 = generateArtifact(TEST_ARTIFACT_1, tDirectory);								
		File targetDirectory1 = UniversalPath.from( targetRoot).push("com.braintribe.devrock.test.t", ".").push("1.0.2").toFile();		
		Artifact target1 = generateArtifact(TEST_ARTIFACT_1, targetDirectory1);
		
		validator.validate( source1, target1, expectHashes);		
		
		Artifact source2 = generateArtifact(TEST_ARTIFACT_2, aDirectory);								
		File targetDirectory2 = UniversalPath.from( targetRoot).push("com.braintribe.devrock.test.a", ".").push("1.0.2").toFile();		
		Artifact target2 = generateArtifact(TEST_ARTIFACT_2, targetDirectory2);
		
		validator.validate( source2, target2, expectHashes);
		
		validator.assertResults();
	}

}
