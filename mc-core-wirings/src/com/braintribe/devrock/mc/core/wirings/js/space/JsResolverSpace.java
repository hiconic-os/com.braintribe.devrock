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
package com.braintribe.devrock.mc.core.wirings.js.space;

import com.braintribe.devrock.mc.core.resolver.js.BasicJsDependencyResolver;
import com.braintribe.devrock.mc.core.resolver.js.BasicJsLibraryLinker;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.DevelopmentEnvironmentContract;
import com.braintribe.devrock.mc.core.wirings.js.contract.JsResolverContract;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

/**
 * implementation of the {@link JsResolverContract}
 * @author pit / dirk
 *
 */
@Managed
public class JsResolverSpace implements JsResolverContract {

	@Import
	private TransitiveResolverContract transitiveResolver;
	
	@Import
	private ArtifactDataResolverContract artifactDataResolver;
	
	@Import
	private VirtualEnvironmentContract virtualEnvironment;
	
	@Import
	private DevelopmentEnvironmentContract developmentEnvironment;
	
	@Override
	public BasicJsLibraryLinker jsLibraryLinker() {
		BasicJsLibraryLinker bean = new BasicJsLibraryLinker();
		bean.setDeclaredArtifactCompiler(artifactDataResolver.declaredArtifactCompiler());
		bean.setJsDependencyResolver(jsResolver());
		bean.setLockProvider(artifactDataResolver.backendContract().lockSupplier());
		bean.setVirtualEnvironment(virtualEnvironment.virtualEnvironment());
		bean.setDevelopmentEnvironmentRoot(developmentEnvironment.developmentEnvironmentRoot());
		return bean;
	}
	
	@Override
	public BasicJsDependencyResolver jsResolver() {
		BasicJsDependencyResolver bean = new BasicJsDependencyResolver();
		bean.setPartDownloadManager(artifactDataResolver.partDownloadManager());
		bean.setRepositoryReflection(artifactDataResolver.repositoryReflection());
		bean.setTransitiveDependencyResolver(transitiveResolver.transitiveDependencyResolver());
		return bean;
	}
	
	@Override
	public TransitiveResolverContract transitiveResolverContract() {	
		return transitiveResolver;
	}
	

}
