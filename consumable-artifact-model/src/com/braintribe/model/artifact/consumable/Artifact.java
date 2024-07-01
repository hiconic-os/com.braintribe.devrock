// ============================================================================
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

import java.util.Map;

import com.braintribe.gm.model.reason.HasFailure;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * represents a high level artifact, i.e. one ready to be 'consumed'
 * @author pit
 *
 */
public interface Artifact extends VersionedArtifactIdentification, HasFailure {
	
	EntityType<Artifact> T = EntityTypes.T(Artifact.class);

	String packaging = "packaging";
	String archetype = "archetype";
	String parts = "parts";
	
	/**
	 * @return - the packaging of the artifact
	 */
	String getPackaging();
	void setPackaging(String packaging);
	
	/**
	 * @return - the archetype (property) of the artifact
	 */
	String getArchetype();
	void setArchetype(String value);
	
	/**
	 * @return - a {@link Map} of the {@link PartIdentification} as {@link String} and the actual {@link Part}
	 */
	Map<String, Part> getParts();
	void setParts( Map<String, Part> parts);
	
	static Artifact from(Artifact other) {
		Artifact artifact = Artifact.T.create();
		artifact.setGroupId(other.getGroupId());
		artifact.setArtifactId(other.getArtifactId());
		artifact.setVersion(other.getVersion());
		artifact.setFailure(other.getFailure());
		artifact.getParts().putAll(other.getParts());
		artifact.setPackaging( other.getPackaging());
		artifact.setArchetype( other.getArchetype());
		return artifact;
	}
}
