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
package com.braintribe.devrock.mc.core.resolver;

import java.util.Optional;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.devrock.mc.api.resolver.DependencyResolver;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

/**
 * a caching implementation of the {@link DependencyResolver}
 * 
 * @author pit/dirk
 *
 */
public class CachingDependencyResolver implements DependencyResolver {

	private LoadingCache<EqProxy<CompiledDependencyIdentification>, Maybe<CompiledArtifactIdentification>> cache;
	private DependencyResolver delegate;
	
	{
		cache = Caffeine.newBuilder().build(this::load);
	}
	
	public CachingDependencyResolver(DependencyResolver delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public Maybe<CompiledArtifactIdentification> resolveDependency( CompiledDependencyIdentification dependencyIdentification) {	
		return cache.get( HashComparators.compiledDependencyIdentification.eqProxy(dependencyIdentification));
	}
	
	/**
	 * @param eqproxy - an {@link EqProxy} of the {@link CompiledDependencyIdentification}
	 * @return - an {@link Optional} containing the {@link CompiledDependencyIdentification} or an empty one if not found
	 */
	private Maybe<CompiledArtifactIdentification> load( EqProxy<CompiledDependencyIdentification> eqproxy) {
		Maybe<CompiledArtifactIdentification> result = delegate.resolveDependency( eqproxy.get());
		return result;
	}

}
