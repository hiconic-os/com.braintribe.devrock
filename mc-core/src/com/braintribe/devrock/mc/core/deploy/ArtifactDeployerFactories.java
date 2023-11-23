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
package com.braintribe.devrock.mc.core.deploy;

import java.util.function.Function;

import com.braintribe.devrock.mc.api.deploy.ArtifactDeployer;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.core.expert.impl.PolymorphicDenotationMap;

/**
 * {@link PolymorphicDenotationMap} for different {@link ArtifactDeployer} based on the type of {@link Repository}
 * @author pit / dirk
 *
 */
public class ArtifactDeployerFactories extends PolymorphicDenotationMap<Repository, Function<Repository, ArtifactDeployer>> {
	/**
	 * register a 'function-style' factory for the specified type 
	 * @param <R> - the actual GE type 
	 * @param type - the {@link EntityType} of R
	 * @param factory - the functional factory to return a {@link ArtifactDeployer} based on the {@link Repository} itself
	 */
	public <R extends Repository> void register(EntityType<R> type, Function<R, ArtifactDeployer> factory) {
		put(type, (Function<Repository, ArtifactDeployer>)factory);
	}
}
