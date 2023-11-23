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
package com.braintribe.devrock.mc.core.wirings.workspace.space;

import com.braintribe.devrock.mc.core.resolver.codebase.RepositoryConfigurationCodebaseEnricher;
import com.braintribe.devrock.mc.core.resolver.workspace.RepositoryConfigurationWorkspaceEnricher;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.RepositoryConfigurationEnrichingContract;
import com.braintribe.devrock.mc.core.wirings.workspace.contract.WorkspaceEnrichingConfigurationContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;
import com.braintribe.wire.api.space.WireSpace;

/**
 * @author pit / dirk
 *
 */
@Managed
public class WorkspaceEnrichingSpace implements WireSpace {
	
	@Import
	private RepositoryConfigurationEnrichingContract repositoryConfigurationEnriching;
	
	@Import
	private WorkspaceEnrichingConfigurationContract workspaceEnrichingConfiguration;
	
	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		repositoryConfigurationEnriching.enrichRepositoryConfiguration(this::repositoryConfigurationEnricher);
	}
	
	/**
	 * @return - instrumented {@link RepositoryConfigurationCodebaseEnricher}
	 */
	@Managed
	private RepositoryConfigurationWorkspaceEnricher repositoryConfigurationEnricher() {
		RepositoryConfigurationWorkspaceEnricher bean = new RepositoryConfigurationWorkspaceEnricher();
		bean.setWorkspaceRepositories(workspaceEnrichingConfiguration.workspaceRepositories());
		return bean;
	}
}
