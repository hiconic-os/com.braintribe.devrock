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

import java.util.Optional;

import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author pit
 *
 */
public interface Relocation extends VersionedArtifactIdentification {
	 
	EntityType<Relocation> T = EntityTypes.T(Relocation.class);
	
	String message = "message";
	
	String getMessage();
	void setMessage(String message);	
	
	static Relocation from( Relocation relocation, VersionedArtifactIdentification vai) {
		return from( relocation, vai.getGroupId(), vai.getArtifactId(), vai.getVersion());
	}
	static Relocation from( Relocation relocation, String groupId, String artifactId, String version) {
		Relocation cr = Relocation.T.create();
		cr.setGroupId(  Optional.ofNullable( relocation.getGroupId()).orElseGet( () -> groupId));
		cr.setArtifactId( Optional.ofNullable( relocation.getArtifactId()).orElseGet( () -> artifactId));
		cr.setVersion( Optional.ofNullable( relocation.getVersion()).orElseGet( () -> version));
		return cr;
	}
}
