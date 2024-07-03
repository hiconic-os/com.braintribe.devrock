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
