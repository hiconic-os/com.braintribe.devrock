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
