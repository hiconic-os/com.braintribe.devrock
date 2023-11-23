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

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.HasMetaData;

/**
 * a dependency .. is more or less a dependency as it appears in the pom, but has additional flags and data used in the
 * analysis or walks.
 * 
 * @author pit
 */
public interface Dependency extends Identification, HasMetaData {

	EntityType<Dependency> T = EntityTypes.T(Dependency.class);

	VersionRange getVersionRange();
	void setVersionRange(VersionRange versionRange);

	Set<Exclusion> getExclusions();
	void setExclusions(Set<Exclusion> exclusions);

	Set<Solution> getSolutions();
	void setSolutions(Set<Solution> solutions);

	boolean getOptional();
	void setOptional(boolean optional);

	/** use {@link #getType()} */
	@Deprecated
	String getPackagingType();
	@Deprecated
	void setPackagingType(String type);

	/** use {@link #getType()} */
	@Deprecated
	String getDependencyType();
	@Deprecated
	void setDependencyType(String type);

	/** the dependency, i.e. the content of the tag type in the pom */
	String getType();
	void setType(String type);

	String getScope();
	void setScope(String scope);

	boolean getUndetermined();
	void setUndetermined(boolean flag);

	boolean getUnresolved();
	void setUnresolved(boolean flag);

	Set<Artifact> getRequestors();
	void setRequestors(Set<Artifact> artifact);

	Integer getPathIndex();
	void setPathIndex(Integer index);

	Integer getHierarchyLevel();
	void setHierarchyLevel(Integer level);

	boolean getExcluded();
	void setExcluded(boolean flag);

	Dependency getOverridingDependency();
	void setOverridingDependency(Dependency override);

	boolean getAutoDefined();
	void setAutoDefined(boolean flag);

	Set<Dependency> getMergeParents();
	void setMergeParents(Set<Dependency> mergeParents);

	String getGroup();
	void setGroup(String group);

	List<String> getTags();
	void setTags(List<String> tags);
	
	Map<String,String> getRedirectionMap();
	void setRedirectionMap( Map<String, String> redirectionMap);
	
	/**
	 * @return - returns true if this {@link Dependency} is marked as invalid (by MC for instance)
	 */
	boolean getIsInvalid();
	void setIsInvalid( boolean isInvalid);
	
	/**
	 * @return - if invalidated, the reason might be here .. 
	 */
	String getInvalidationReason();
	void setInvalidationReason( String reason);

}
