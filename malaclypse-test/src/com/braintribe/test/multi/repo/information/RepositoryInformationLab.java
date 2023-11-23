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
