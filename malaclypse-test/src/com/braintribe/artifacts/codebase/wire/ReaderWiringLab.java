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
