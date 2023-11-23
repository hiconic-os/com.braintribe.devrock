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
package com.braintribe.model.artifact.compiled;

import com.braintribe.gm.model.reason.HasFailure;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Is a container for a dependency and its solution or a failure if that solution could not be found 
 * 
 * @author pit / dirk 
 *
 */
public interface CompiledSolution extends HasFailure {
	
	EntityType<CompiledSolution> T = EntityTypes.T(CompiledSolution.class);
	
	String solution = "solution";
	String dependency = "dependency";

	CompiledDependencyIdentification getDependency();
	void setDependency(CompiledDependencyIdentification dependency);
	
	/**
	 * @return - the {@link CompiledArtifact} which is the 'solution' to this dependency
	 */
	CompiledArtifact getSolution();
	void setSolution(CompiledArtifact value);
}
