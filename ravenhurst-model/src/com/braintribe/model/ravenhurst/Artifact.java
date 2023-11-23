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
package com.braintribe.model.ravenhurst;

import java.util.Set;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface Artifact extends StandardIdentifiable {

	final EntityType<Artifact> T = EntityTypes.T(Artifact.class);

	public String getGroupId();
	public void setGroupId(String value);

	public String getArtifactId();
	public void setArtifactId(String value);

	public String getVersion();
	public void setVersion(String value);

	public Set<Part> getParts();
	public void setParts(Set<Part> value);

	public boolean getRedeployedFlag();
	public void setRedeployedFlag(boolean flag);

	public boolean getSnapshotFlag();
	public void setSnapshotFlag(boolean flag);

}
