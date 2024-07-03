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
package com.braintribe.devrock.artifact.test;

import java.io.File;
import java.util.List;

import org.assertj.core.util.Lists;

import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.resolver.ArtifactDataResolverModule;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;

public enum ArtifactReflectionGeneratorTestWireModule implements WireTerminalModule<ArtifactDataResolverContract> {
	
	INSTANCE;
	
	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		RepositoryConfiguration repositoryConfiguration = RepositoryConfiguration.T.create();
		repositoryConfiguration.setLocalRepositoryPath(new File("test-output/local-repo").getAbsolutePath());
		
		contextBuilder.bindContract(RepositoryConfigurationContract.class, () -> Maybe.complete(repositoryConfiguration));
	}
	
	@Override
	public List<WireModule> dependencies() {
		return Lists.list(ArtifactDataResolverModule.INSTANCE);
	}

}
