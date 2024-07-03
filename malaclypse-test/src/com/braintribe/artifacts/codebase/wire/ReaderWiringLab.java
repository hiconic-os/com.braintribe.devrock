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
package com.braintribe.artifacts.codebase.wire;

import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.representations.artifact.maven.settings.OverrideableVirtualEnvironment;
import com.braintribe.build.artifacts.mc.wire.pomreader.contract.ConfigurablePomReaderExternalContract;
import com.braintribe.build.artifacts.mc.wire.pomreader.contract.PomReaderContract;
import com.braintribe.build.artifacts.mc.wire.pomreader.external.contract.PomReaderExternalContract;
import com.braintribe.model.malaclypse.cfg.repository.RemoteRepository;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

@Category(KnownIssue.class)
public class ReaderWiringLab {
	
	
	private static final String PROFILE_USECASE = "PROFILE_USECASE";
	private static final String useCase ="DEVROCK";

	@Test
	public void test() {
		RemoteRepository remo = lookupViaReader("devrock");
		System.out.println(remo);
	}

	private RemoteRepository lookupViaReader( String refid) {
		
		// 		
		ConfigurablePomReaderExternalContract ec = new ConfigurablePomReaderExternalContract();
		OverrideableVirtualEnvironment ove = new OverrideableVirtualEnvironment();
		ove.addEnvironmentOverride( PROFILE_USECASE, useCase);
		ec.setVirtualEnvironment( ove);
			
		
		WireContext<PomReaderContract> wireContext = Wire.context( PomReaderContract.class)
				.bindContracts("com.braintribe.build.artifacts.mc.wire.pomreader")
				.bindContract( PomReaderExternalContract.class, ec)
				.build();
		MavenSettingsReader reader = wireContext.beans().settingsReader();
		
		List<RemoteRepository> activeRemoteRepositories = reader.getActiveRemoteRepositories();
		for (RemoteRepository remoteRepo : activeRemoteRepositories) {
			String id = remoteRepo.getId();
			if (refid.equalsIgnoreCase(id)) {						
				return remoteRepo;				
			}
		}
		System.err.println("no remote repository with id [" + refid + "] found within profiles activated by [" + PROFILE_USECASE + "] set to [" + useCase + "]");
		
		return null;
		
	}
}
