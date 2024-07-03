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
package com.braintribe.devrock.mc.core.wired.resolving.access;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.testing.category.KnownIssue;

/**
 * tests the ranged access: the dependency is a ranged dependency (dependency within t#1.0.1),
 * downloads should happen (downloading maven-metadata is required for ranged access) 
 * 
 * @author pit
 *
 */
@Category( KnownIssue.class)
public class RangedAccessTest extends AbstractAccessWhileResolvingTest {

	@Override
	protected RepoletContent archiveInput() {
		return archiveContent(expressiveContentFile);
	}

	@Test
	public void run() {
		run( GRP + ":t#1.0.2", standardResolutionContext);
		// check downloads
		List<String> downloadedFiles = downloadsNotified.get( RepositoryName);
		if (downloadedFiles == null || downloadedFiles.size() == 0) {
			Assert.fail("unexpectedly, no files have been downloaded");
		}
	}

}
