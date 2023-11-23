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
