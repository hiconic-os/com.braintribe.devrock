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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.upload;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.model.artifact.consumable.ArtifactResolution;

/**
 * tests that upload return values < 200, > 300 are reported as failed and incorporated into the resolution..
 * 
 * @author pit
 *
 */
public class UploadWithUnexpectedResponsesTest extends AbstractUploadTest {
	// repolet has been instructed to return 409 for the pom of this artifact
	private static final String TEST_ARTIFACT = "com.braintribe.devrock.test:c#1.0.2";

	@Override
	protected RepoletContent archiveInput() {	
		return RepoletContent.T.create();
	}

	private void run(String repositoryId, File targetRoot, boolean expectHashes) {
		File directory = new File( input, "c-1.0.2");
		ArtifactResolution resolution = runSingle( TEST_ARTIFACT, directory, repositoryId);
		
		if (resolution.hasFailed()) {
			//upload failed : " + resolution.getFailure().asFormattedText()
		}
		else { 
			Assert.fail("expected upload to fail, but it didn't");
		}
		
		/*		
		// validate expectations
		Artifact source = generateArtifact(TEST_ARTIFACT, directory);
								
		File targetDirectory = UniversalPath.from(targetRoot).push("com.braintribe.devrock.test.c", ".").push("1.0.2").toFile();		
		Artifact target = generateArtifact(TEST_ARTIFACT, targetDirectory);
		
		Validator validator = new Validator();		
		validator.validate( source, target, expectHashes);		
		validator.assertResults();
		*/
	}

	@Test
	public void runSingleUploadTest() {		
		run( "archive", upload, true);				
	}
	

}
