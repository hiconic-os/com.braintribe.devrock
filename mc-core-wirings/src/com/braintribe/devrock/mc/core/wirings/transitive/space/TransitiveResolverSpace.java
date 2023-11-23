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
		
		return bean;
	}

	@Override
	public ArtifactDataResolverContract dataResolverContract() {	
		return artifactDataResolver;
	}
	
	
}
