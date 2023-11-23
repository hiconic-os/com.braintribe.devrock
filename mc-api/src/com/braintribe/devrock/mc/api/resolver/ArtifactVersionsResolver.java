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
