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
package com.braintribe.model.artifact.consumable;

import java.util.List;

import com.braintribe.gm.model.reason.HasFailure;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * common result for the transitive resolvers (direct or consumers)
 * @author pit / dirk
 *
 */
public interface ArtifactResolution extends HasFailure {
	
	EntityType<ArtifactResolution> T = EntityTypes.T(ArtifactResolution.class);
	
	String terminals = "terminals";
	String solutions = "solutions";

	List<Artifact> getTerminals();
	void setTerminals(List<Artifact> terminals);
	
	List<Artifact> getSolutions();
	void setSolutions(List<Artifact> solutions);
}
