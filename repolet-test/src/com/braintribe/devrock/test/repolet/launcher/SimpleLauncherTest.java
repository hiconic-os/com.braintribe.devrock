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
