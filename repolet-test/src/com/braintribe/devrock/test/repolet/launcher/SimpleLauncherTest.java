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
package com.braintribe.devrock.test.repolet.launcher;

import java.io.File;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class SimpleLauncherTest extends AbstractFolderBasedRepoletTest {

	
	@Override
	protected File getRoot() {		
		return new File( res, "simple");
	}
	
	@Test 
	public void test() {
		Map<String, String> launchedRepolets = launcher.getLaunchedRepolets();
		Assert.assertTrue( "expected two repolets, found [" + launchedRepolets.size() + "]", launchedRepolets.size() == 2);
		
		String basicUrl = "http://localhost:${env.port}/".replace("${env.port}", "" + launcher.getAssignedPort());
		
		
		for (Map.Entry<String, String> entry : launchedRepolets.entrySet()) {
			String name = entry.getKey();
			String url = entry.getValue();
			String expected = basicUrl + name;
			Assert.assertTrue("expected url [" + expected + "], yet found [" + url + "]", expected.equals( url));
		}
	}
		
}
