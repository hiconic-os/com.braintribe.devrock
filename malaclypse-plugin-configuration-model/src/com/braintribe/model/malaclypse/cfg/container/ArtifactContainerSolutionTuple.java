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
