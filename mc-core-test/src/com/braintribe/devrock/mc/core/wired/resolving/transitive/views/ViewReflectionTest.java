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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.views;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.model.repositoryview.resolution.RepositoryViewResolution;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

public class ViewReflectionTest extends AbstractViewResolvingTest {
	
	@Override
	protected RepoletContent archiveInput() {
		return archiveInput( "archive.definition.yaml");
	}
	
	@Test
	public void runViewReflectionTest() {
		try {
			RepositoryReflection reflection = getReflection();

			RepositoryViewResolution repositoryViewResolution = reflection.getRepositoryViewResolution();
			Assertions.assertThat(repositoryViewResolution.getSolutions().size()).isEqualTo(1);
			Assertions.assertThat(repositoryViewResolution.getTerminals().size()).isEqualTo(1);
			Assertions.assertThat(repositoryViewResolution.getTerminals().get(0)).isSameAs(repositoryViewResolution.getTerminals().get(0));
 
			// validate : 
			// local repository
			// remote : archive, with filter
			System.out.println(reflection);
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("unexpected exception [" + e.getMessage() + "]");
			
		}
		System.out.println();
	}
	
}
