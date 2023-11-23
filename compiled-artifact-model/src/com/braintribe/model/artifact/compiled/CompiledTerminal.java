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

import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * the entry point for the resolvers, the TDR and the CPR
 * @author pit/dirk
 *
 */
@Abstract
public interface CompiledTerminal extends ArtifactIdentification {
	
	EntityType<CompiledTerminal> T = EntityTypes.T(CompiledTerminal.class);

	/**
	 * @param dependency - {@link CompiledDependencyIdentification}
	 * @return - the  resulting {@link CompiledDependency}-based {@link CompiledTerminal}
	 */
	static CompiledTerminal from(CompiledDependencyIdentification dependency) {
		return from(CompiledDependency.from(dependency));
	}
	
	/**
	 * @param dependency - a {@link CompiledDependency}
	 * @return - the  resulting {@link CompiledDependency}-based {@link CompiledTerminal}
	 */
	static CompiledTerminal from(CompiledDependency dependency) {
		return dependency;
	}
	
	/**
	 * @param artifact - a {@link CompiledArtifact}
	 * @return - the  resulting {@link CompiledArtifact}-based {@link CompiledTerminal} 
	 */
	static CompiledTerminal from(CompiledArtifact artifact) {
		return artifact;
	}
	
	/**
	 * @param artifact - a {@link VersionedArtifactIdentification}
	 * @return - the  resulting {@link CompiledDependency}-based {@link CompiledTerminal}
	 */
	static CompiledTerminal from(VersionedArtifactIdentification artifact) {
		return from(CompiledDependency.from(artifact));
	}
	
	/**
	 * @param terminal - a string representation of a {@link CompiledDependencyIdentification}
	 * @return - the  resulting {@link CompiledDependency}-based {@link CompiledTerminal}
	 */
	static CompiledTerminal parse(String terminal) {
		return from(CompiledDependencyIdentification.parse(terminal));
	}
	
	/**
	 * @param groupId 
	 * @param artifactId
	 * @param version
	 * @return - the  resulting {@link CompiledDependency}-based {@link CompiledTerminal}
	 */
	static CompiledTerminal create(String groupId, String artifactId, String version) {
		return from(CompiledArtifactIdentification.create(groupId, artifactId, version));
	}

	/**
	 * @param artifactIdentification - a {@link CompiledArtifactIdentification}
	 * @return - the  resulting {@link CompiledDependency}-based {@link CompiledTerminal}
	 */
	static CompiledTerminal from(CompiledArtifactIdentification artifactIdentification) {
		return CompiledDependency.create(artifactIdentification.getGroupId(), artifactIdentification.getArtifactId(), artifactIdentification.getVersion(), "compile");
	}
}
