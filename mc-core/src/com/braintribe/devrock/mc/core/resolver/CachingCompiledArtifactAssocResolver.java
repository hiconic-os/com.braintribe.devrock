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

import java.util.function.Predicate;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.braintribe.caffeine.ValuePredicateStalingCacheExpiry;
import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.mc.api.resolver.CompiledArtifactIdentificationAssocResolver;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.utils.lcd.LazyInitialized;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;


/**
 *  a {@link CachingCompiledArtifactAssocResolver} can cache different types linked to a {@link CompiledArtifactIdentification}
 * 
 * @author pit/dirk
 *
 */
public class CachingCompiledArtifactAssocResolver<V> implements CompiledArtifactIdentificationAssocResolver<V>, InitializationAware {
	private LoadingCache<EqProxy<CompiledArtifactIdentification>,LazyInitialized<Maybe<V>>> cache;
	private CompiledArtifactIdentificationAssocResolver<V> delegate;
	private Predicate<? super V> updateFilter;
	
	/**
	 * @param delegate - the {@link CompiledArtifactIdentificationAssocResolver} to use if it's not in cache
	 */
	@Configurable @Required
	public void setDelegate(CompiledArtifactIdentificationAssocResolver<V> delegate) {
		this.delegate = delegate;
	}
	
	/**
	 * @param updateFilter - a filter to decide whether the cached element needs to be refreshed, default never
	 */
	@Configurable
	public void setUpdateFilter(Predicate<? super V> updateFilter) {
		this.updateFilter = updateFilter;
	}
	
	@Override
	public void postConstruct() {
		@NonNull
		Caffeine<Object, Object> builder = Caffeine.newBuilder();
		if (updateFilter != null) {
			builder.expireAfter( new ValuePredicateStalingCacheExpiry<>( updateFilter));
		}
		cache = builder.build( this::delegateResolve);		
	}
	
	/**
	 * caffeine cache loader function 
	 * @param eqProxy - the {@link EqProxy} containing the {@link CompiledArtifactIdentification}
	 * @return - V
	 */
	private LazyInitialized<Maybe<V>> delegateResolve( EqProxy<CompiledArtifactIdentification> eqProxy) {
		return new LazyInitialized<>(() -> delegate.resolve( eqProxy.get()));
	}
	
	
	@Override
	public Maybe<V> resolve(CompiledArtifactIdentification artifactIdentification) {
		EqProxy<CompiledArtifactIdentification> key = HashComparators.compiledArtifactIdentification.eqProxy(artifactIdentification);
		Maybe<V> v = cache.get( key).get();
		return v; 
	}
	
	
}
