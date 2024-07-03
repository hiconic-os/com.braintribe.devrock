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
package com.braintribe.devrock.mc.api.resolver;

import java.util.List;

import com.braintribe.devrock.mc.api.commons.VersionInfo;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.essential.ArtifactIdentification;


/**
 * a resolver that can tell us what version of an artifact exists 
 * @author pit / dirk
 *
 */
public interface ArtifactVersionsResolver {
	/**
	 * @param artifactIdentification - the {@link ArtifactIdentification} that identifies the artifact (family)
	 * @return - a {@link List} of {@link VersionInfo} that reflects the versions of the artifact passed
	 */
	List<VersionInfo> getVersions( ArtifactIdentification artifactIdentification); 
	
	Maybe<List<VersionInfo>> getVersionsReasoned( ArtifactIdentification artifactIdentification); 
}
