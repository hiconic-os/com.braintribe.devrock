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

import com.braintribe.gm.model.reason.HasFailure;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * common result for the transitive resolvers (direct or consumers)
 * @author pit / dirk
 *
 */
public interface ArtifactResolution extends HasFailure {
	
	EntityType<ArtifactResolution> T = EntityTypes.T(ArtifactResolution.class);
	
	String terminals = "terminals";
	String solutions = "solutions";

	List<Artifact> getTerminals();
	void setTerminals(List<Artifact> terminals);
	
	List<Artifact> getSolutions();
	void setSolutions(List<Artifact> solutions);
}
