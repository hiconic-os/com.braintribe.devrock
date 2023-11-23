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
package com.braintribe.devrock.mc.core.wirings.resolver.space;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.devrock.mc.core.wirings.resolver.contract.RepositoryConfigurationEnrichingContract;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class RepositoryConfigurationEnrichingSpace implements RepositoryConfigurationEnrichingContract {

	@Import
	ArtifactDataResolverSpace artifactDataResolver;

	@Override
	public void enrichRepositoryConfiguration(Supplier<Consumer<RepositoryConfiguration>> enricherSupplier) {
		artifactDataResolver.repositoryConfigurationEnriching().addEnricher(enricherSupplier);
	}
}
