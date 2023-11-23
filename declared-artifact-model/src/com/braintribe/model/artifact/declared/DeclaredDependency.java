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
