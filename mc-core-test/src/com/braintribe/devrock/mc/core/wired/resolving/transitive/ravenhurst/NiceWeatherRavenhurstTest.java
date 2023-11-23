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
 * simple test that tests that RH processing has no issues with a correct last-changes-access file
 *  
 * @author pit
 *
 */
public class NiceWeatherRavenhurstTest extends AbstractRavenhurstTest {
	
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
		try {
			copyAndPatch( new File(input, "last-changes-access-archive.normal.yaml"), "last-changes-access-archive.yaml");
			run( "com.braintribe.devrock.test:t#1.0.1", standardTransitiveResolutionContext, true);
		} catch (Exception e) {
			Assert.fail("exception thrown :" + e.getMessage());
		}
	}

}
