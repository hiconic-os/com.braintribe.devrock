// ============================================================================
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
package com.braintribe.artifacts.test.publishing;

import java.io.File;

import org.junit.Assert;
import org.junit.experimental.categories.Category;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionHelper;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.testing.category.KnownIssue;

@Category(KnownIssue.class)
public class PublishingNamingTest {
	private static String workingCopyLocation = System.getenv( "BT__ARTIFACTS_HOME");

	
	private String determineLocation( Part pomPart) {
	
		try {
			return RepositoryReflectionHelper.getHotfixSavySolutionFilesystemLocation( workingCopyLocation, pomPart) + File.separator + "pom.xml";
		} catch (RavenhurstException e) {
			Assert.fail("Exception [" + e.getMessage() + "] thrown");
			return null;
		}
	}

	
	//@Test
	public void testHotfixNaming() {
		try {
			Part part = Part.T.create();
			part.setGroupId("com.braintribe.test");
			part.setArtifactId("Test");
			
			part.setVersion( VersionProcessor.createFromString( "1.0"));			
			String location = determineLocation(part);			
			
			part.setVersion( VersionProcessor.createFromString( "1.0.1"));
			String hotfixLocation = determineLocation(part);
			
			Assert.assertTrue("[" + location + "] expected for hotfix, but [" + hotfixLocation + "] found", location.equalsIgnoreCase(hotfixLocation));
			
		} catch (Exception e) {
			Assert.fail("Exception [" + e.getMessage() + "] thrown");
		}
		
	}
	
}
