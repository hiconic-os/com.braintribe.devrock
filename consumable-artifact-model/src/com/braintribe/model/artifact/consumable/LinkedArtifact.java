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
