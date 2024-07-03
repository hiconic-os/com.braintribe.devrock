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
package com.braintribe.test.multi.repo.information;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RetrievalMode;
import com.braintribe.model.artifact.info.ArtifactInformation;
import com.braintribe.testing.category.KnownIssue;

@Category(KnownIssue.class)
public class RepositoryInformationLab extends AbstractRepositoryInformationRetrievalTest {
	private static File settings = new File( contents, "settings.user.xml");
	private static File localRepository = new File( contents, "repo");
	
	@BeforeClass 
	public static void before() {
		//TestUtil.ensure( localRepository);
		
		before(settings, localRepository);
	}

	@Test
	public void testMC() {
		ArtifactInformation info = test( "com.braintribe.test.dependencies.subtreeexclusiontest:A#1.0", RetrievalMode.passive);
		dump( info);
	}
}
