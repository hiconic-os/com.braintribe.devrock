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
package com.braintribe.devrock.mc.api.resolver;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;

/**
 * the basic interface for the different artifact resolvers (also on declared level)
 * @author pit / dirk
 *
 * @param <V> - the actual type that is returned 
 */
public interface CompiledArtifactIdentificationAssocResolver<V> {
	 /**
	 * @param compiledArtifactIdentification - the {@link CompiledArtifactIdentification} to resolve 
	 * @return - what ever it returns
	 */
	Maybe<V> resolve( CompiledArtifactIdentification compiledArtifactIdentification);
}
