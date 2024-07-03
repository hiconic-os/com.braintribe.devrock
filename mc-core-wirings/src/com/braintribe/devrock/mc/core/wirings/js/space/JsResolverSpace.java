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
