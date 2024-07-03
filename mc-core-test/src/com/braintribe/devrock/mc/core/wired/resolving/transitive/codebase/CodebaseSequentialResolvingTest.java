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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.codebase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * tests that within injected codebase repositories, the order of their appearance in the configuration is respected
 * 
 * @author pit
 *
 */
public class CodebaseSequentialResolvingTest extends AbstractCodebaseClasspathResolvingTest {
	
	private List<Pair<File,Boolean>> filesToCheckOnExistance = new ArrayList<>();
	{
		filesToCheckOnExistance.add( Pair.of( new File( repo, "last-probing-result-archive.yaml"), true));
	}

	@Override
	protected RepoletContent archiveInput() {	
		return RepoletContent.T.create();
	}

	
	@Test
	public void testDominanceInSequence() {
	
		Pair<File,String> p1 = Pair.of( new File( input, "multiple/codebase-1"), "${artifactId}");
		Pair<File,String> p2 = Pair.of( new File( input, "multiple/codebase-2"), "${artifactId}");
		
		AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#1.0.1", ClasspathResolutionContext.build().enrichJar(false).done(), p1, p2);
		
		Validator validator = new Validator(true);
		validator.validate(new File(input, "multiple/multiple.plain.validation.txt"), resolution);
		validator.validateFileExistance( filesToCheckOnExistance);
		//validator.validateFileExistance( metadataToCheckOnExistance);
		validator.assertResults();		
	}

}
