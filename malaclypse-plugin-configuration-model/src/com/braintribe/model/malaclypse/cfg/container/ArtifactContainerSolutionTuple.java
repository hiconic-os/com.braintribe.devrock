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
package com.braintribe.model.malaclypse.cfg.container;

import java.util.Date;
import java.util.List;

import com.braintribe.model.artifact.Solution;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface ArtifactContainerSolutionTuple extends GenericEntity{

	final EntityType<ArtifactContainerSolutionTuple> T = EntityTypes.T(ArtifactContainerSolutionTuple.class);
	
	void setTimestamp( Date date);
	Date getTimestamp();
	
	void setMd5( String md5);
	String getMd5();
	
	void setCompileSolutions( List<Solution> compileSolutions);
	List<Solution> getCompileSolutions();
	
	void setLaunchSolutions( List<Solution> launchSolutions);
	List<Solution> getLaunchSolutions();

}
