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
