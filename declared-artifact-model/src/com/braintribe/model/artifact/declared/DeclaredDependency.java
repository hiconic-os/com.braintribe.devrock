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
package com.braintribe.model.artifact.declared;

import java.util.List;
import java.util.Set;

import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.DependencyIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


/**
 * a dependency as stated in the pom
 * @author pit
 *
 */
public interface DeclaredDependency extends DependencyIdentification, VersionedArtifactIdentification {
		
	String scope = "scope";
	String optional = "optional";
	String processingInstructions = "processingInstructions";
	String origin = "origin";
	String exclusions = "exclusions";
	
	EntityType<DeclaredDependency> T = EntityTypes.T(DeclaredDependency.class);
	
	/**	 
	 * @return - the scope as declared in the pom
	 */
	String getScope();
	void setScope(String scope);
	
	/**
	 * @return - the optional flag as declared in the pom
	 */
	Boolean getOptional();
	void setOptional(Boolean optional);
	
	/**
	 * 
	 * @return - processing instructions attached the dependency in the pom
	 */
	List<ProcessingInstruction> getProcessingInstructions();
	void setProcessingInstructions(List<ProcessingInstruction> instructions);

	/**
	 * 
	 * @return - the {@link DeclaredArtifact} that contains this dependency
	 */
	DeclaredArtifact getOrigin();
	void setOrigin( DeclaredArtifact origin);
	
	
	/**
	 * @return - the exclusions of the dependency 
	 */
	Set<ArtifactIdentification> getExclusions();
	void setExclusions( Set<ArtifactIdentification> exclusions);
	

	@Override
	default String asString() {
		return VersionedArtifactIdentification.super.asString();
	}
}
