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
package com.braintribe.devrock.model.mc.reason;

import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@SelectiveInformation("Artifact identified itself as ${artifact} in contrast to its address ${address}.")
public interface ArtifactIdentificationMismatch extends HasArtifact, McReason {
	
	EntityType<ArtifactIdentificationMismatch> T = EntityTypes.T(ArtifactIdentificationMismatch.class);
	
	String address = "address";
	
	CompiledArtifactIdentification getAddress();
	void setAddress(CompiledArtifactIdentification address);
	
}
