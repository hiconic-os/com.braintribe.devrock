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
