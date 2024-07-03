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
package com.braintribe.test.multi.repo.connectivity;

import java.io.File;

import com.braintribe.artifacts.test.maven.framework.FakeLocalRepositoryProvider;
import com.braintribe.artifacts.test.maven.framework.FakeMavenSettingsPersistenceExpertImpl;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.CrcValidationLevel;
import com.braintribe.build.artifact.test.repolet.LauncherShell;
import com.braintribe.test.framework.TestUtil;
import com.braintribe.test.multi.AbstractWalkLab;

public abstract class AbstractConnectivityMultiRepoWalkLab extends AbstractWalkLab {	
	protected static File localRepository;
	protected static LauncherShell launcherShell;
	
	//protected static File contents; 
	protected static File [] data; 
			
	public static int before(File settings, File localRepository) {
		return before( settings, localRepository, CrcValidationLevel.ignore);
	}
	public static int before(File settings, File localRepository, CrcValidationLevel crcValidationLevel) {
		settingsPersistenceExpert = new FakeMavenSettingsPersistenceExpertImpl( settings);
		localRepositoryLocationProvider = new FakeLocalRepositoryProvider( localRepository);
		localRepository.mkdirs();
		int port = runBefore(crcValidationLevel);
		
		// clean local repository
		TestUtil.delete(localRepository);
		
		return port;
	}
	
	public static void after() {
		runAfter();
	}
		
}
