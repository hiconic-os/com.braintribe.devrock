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
package com.braintribe.devrock.mc.api.compiled;

import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

/**
 * {@link DeclaredArtifactCompilingNodeResolver} are used during 'artifact compiling' aka 'pom compiling'
 * @author pit / dirk
 *
 */
public interface DeclaredArtifactCompilingNodeResolver {
	/**
	 * @param versionedArtifactIdentification - the {@link VersionedArtifactIdentification} that identifies the pom
	 * @return - a {@link DeclaredArtifactCompilingNode} that represents the pom 
	 */
	DeclaredArtifactCompilingNode resolveCompilingNode( VersionedArtifactIdentification versionedArtifactIdentification);
}
