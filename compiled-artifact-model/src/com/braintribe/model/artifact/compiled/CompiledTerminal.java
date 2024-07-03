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
