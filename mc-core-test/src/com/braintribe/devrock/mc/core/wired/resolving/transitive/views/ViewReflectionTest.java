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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.views;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.model.repolet.content.RepoletContent;

public class ViewReflectionTest extends AbstractViewResolvingTest {
	
	@Override
	protected RepoletContent archiveInput() {
		return archiveInput( "archive.definition.yaml");
	}
	
	@Test
	public void runViewReflectionTest() {
		try {
			RepositoryReflection reflection = getReflection();
 
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
