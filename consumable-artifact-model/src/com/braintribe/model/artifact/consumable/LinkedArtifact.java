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

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * represents an artifact with references to its dependencies and its respective dependers (requesters)
 * @author pit
 *
 */
public interface LinkedArtifact extends Artifact {
	
	EntityType<LinkedArtifact> T = EntityTypes.T(LinkedArtifact.class);

	
	/**
	 * @return - the dependencies as list of {@link LinkedArtifact}
	 */
	List<LinkedArtifact> getDependencies();
	void setDependencies( List<LinkedArtifact> dependencies);
	
	/**
	 * @return - the dependers (requesters) as list of {@link LinkedArtifact}
	 */
	List<LinkedArtifact> getDependers();
	void setDependers( List<LinkedArtifact> dependers);
	
	static LinkedArtifact from(Artifact other) {
		LinkedArtifact artifact = LinkedArtifact.T.create();
		artifact.setGroupId(other.getGroupId());
		artifact.setArtifactId(other.getArtifactId());
		artifact.setVersion(other.getVersion());
		artifact.setFailure(other.getFailure());
		artifact.getParts().putAll(other.getParts());
		return artifact;
	}
}
