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
package com.braintribe.devrock.mc.core.wirings.view.space;

import com.braintribe.devrock.mc.api.view.RepositoryViewResolver;
import com.braintribe.devrock.mc.core.view.BasicRepositoryViewResolver;
import com.braintribe.devrock.mc.core.wirings.backend.contract.ArtifactDataBackendContract;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.mc.core.wirings.view.contract.RepositoryViewResolutionContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class RepositoryViewResolutionSpace implements RepositoryViewResolutionContract {
	
	@Import
	private TransitiveResolverContract transitiveResolver;
	
	@Import
	private ArtifactDataBackendContract artifactDataBackend;
	
	@Override
	@Managed
	public RepositoryViewResolver repositoryViewResolver() {
		BasicRepositoryViewResolver bean = new BasicRepositoryViewResolver();
		
		bean.setTransitiveDependencyResolver(transitiveResolver.transitiveDependencyResolver());
		bean.setLockSupplier(artifactDataBackend.lockSupplier());
		bean.setPartEnricher(transitiveResolver.dataResolverContract().partEnricher());
		
		return bean;
	}
}
