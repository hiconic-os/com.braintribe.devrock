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
package com.braintribe.devrock.mc.core.wirings.workspace;

import java.util.Collections;
import java.util.List;

import com.braintribe.devrock.mc.core.wirings.backend.ArtifactDataBackendModule;
import com.braintribe.devrock.mc.core.wirings.workspace.contract.WorkspaceEnrichingConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.workspace.space.WorkspaceEnrichingSpace;
import com.braintribe.devrock.model.repository.WorkspaceRepository;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;

/**
 * 
 * @author pit/dirk
 *
 */
public class WorkspaceRepositoryModule implements WireModule {

	private List<WorkspaceRepository> workspaceRepositories;
	

	public WorkspaceRepositoryModule(List<WorkspaceRepository> repositories) {
		workspaceRepositories = repositories;						
	}
			
	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		WireModule.super.configureContext(contextBuilder);
		contextBuilder.bindContract(WorkspaceEnrichingConfigurationContract.class, () -> workspaceRepositories);
		contextBuilder.autoLoad(WorkspaceEnrichingSpace.class);
	}
	
	@Override
	public List<WireModule> dependencies() {
		return Collections.singletonList(ArtifactDataBackendModule.INSTANCE);
	}
	
}
