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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.ravenhurst;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.repolet.launcher.Launcher;

/**
 * simple test that tests that RH processing with a corrupt last-changes-access properly leads to an exception 
 * @author pit
 *
 */
public class CorruptDataRavenhurstTest extends AbstractRavenhurstTest {
	
	@Override
	protected Launcher launcher() {			 
		Launcher launcher = Launcher.build()
				.repolet()
				.name("archive")					
					.changesUrl("http://localhost:${port}/archive/rest/changes")
					.descriptiveContent()
						.descriptiveContent( archiveInput(new File( input, "archive.definition.stage.1.yaml")))
					.close()
				.close()
			.done();		
		return launcher;
	}
	

	@Test
	public void runTest() {
		boolean exceptionThrown = false;
		try {
			copyAndPatch( new File(input, "last-changes-access-archive.corrupt.yaml"), "last-changes-access-archive.yaml");
			run( "com.braintribe.devrock.test:t#1.0.1", standardTransitiveResolutionContext, true);
		} catch (Exception e) {
			exceptionThrown = true;
		}
		if (!exceptionThrown) {
			Assert.fail("expected exception is not thrown");			
		}
	}

}
