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
