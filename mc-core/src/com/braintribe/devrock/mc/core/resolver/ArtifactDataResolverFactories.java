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

import java.util.function.Function;

import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolver;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.core.expert.impl.PolymorphicDenotationMap;

/**
 * {@link PolymorphicDenotationMap} for different {@link ArtifactDataResolver} based on the type of {@link Repository}
 * @author pit / dirk
 *
 */
public class ArtifactDataResolverFactories extends PolymorphicDenotationMap<Repository, Function<Repository, ArtifactDataResolver>> {
	/**
	 * register a 'function-style' factory for the specified type 
	 * @param <R> - the actual GE type 
	 * @param type - the {@link EntityType} of R
	 * @param factory - the functional factory to return a {@link ArtifactDataResolver} based on the {@link Repository} itself
	 */
	public <R extends Repository> void register(EntityType<R> type, Function<R, ArtifactDataResolver> factory) {
		put(type, (Function<Repository, ArtifactDataResolver>)factory);
	}
}
