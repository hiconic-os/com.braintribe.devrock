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
package com.braintribe.model.artifact;

import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.HasMetaData;

/**
 * a solution is a dependency that REALLY exists (whereas a dependency only is a
 * declaration of it)
 * 
 * @author pit
 *
 */

public interface Solution extends Artifact, HasMetaData {

	final EntityType<Solution> T = EntityTypes.T(Solution.class);

	/**	 
	 * will be dropped without replacement as it says little, does less, means nothing
	 * @return
	 */
	@Deprecated
	public Artifact getArtifact();

	/**
	 * will be dropped without replacement as it says little, does less, means nothing
	 * @param artifact
	 */
	@Deprecated
	public void setArtifact(Artifact artifact);

	public Set<Dependency> getRequestors();

	public void setRequestors(Set<Dependency> dependency);

	public boolean getUnresolved();

	public void setUnresolved(boolean flag);

	public Integer getOrder();

	public void setOrder(Integer order);

	public boolean getCorrupt();

	public void setCorrupt(boolean flag);

	public boolean getAggregator();

	public void setAggregator(boolean flag);

	int getHierarchyLevel();

	void setHierarchyLevel(int hierarchyLevel);
}
