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
package com.braintribe.devrock.mc.core.wirings.transitive.space;

import com.braintribe.devrock.mc.core.resolver.transitive.BasicTransitiveDependencyResolver;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

/**
 * @author pit / dirk
 *
 */
@Managed
public class TransitiveResolverSpace implements TransitiveResolverContract {
	
	@Import
	private ArtifactDataResolverContract artifactDataResolver;
	
	@Override
	@Managed
	public BasicTransitiveDependencyResolver transitiveDependencyResolver() {
		BasicTransitiveDependencyResolver bean = new BasicTransitiveDependencyResolver();
		
		bean.setDirectArtifactResolver(artifactDataResolver.directCompiledArtifactResolver());
		bean.setRedirectAwareArtifactResolver(artifactDataResolver.redirectAwareCompiledArtifactResolver());
		bean.setDependencyResolver(artifactDataResolver.dependencyResolver());
		bean.setArtifactDataResolver(artifactDataResolver.artifactResolver());
		bean.setPartEnricher(artifactDataResolver.partEnricher());
		
		if (!artifactDataResolver.parallelResolvingEnabled())
			bean.setThreadPoolSize(1);
		
		return bean;
	}

	@Override
	public ArtifactDataResolverContract dataResolverContract() {	
		return artifactDataResolver;
	}
	
	
}
