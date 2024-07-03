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
package com.braintribe.model.malaclypse.container;

import java.util.Date;
import java.util.List;

import com.braintribe.model.artifact.Solution;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface ContainerPersistence extends GenericEntity{
	
	final EntityType<ContainerPersistence> T = EntityTypes.T(ContainerPersistence.class);

	void setMd5( String md5);
	String getMd5();
	
	void setTimestamp( Date date);
	Date getTimestamp();
	
	void setTerminal( Solution solution);
	Solution getTerminal();
	
	void setCompileSolutions( List<Solution> solutions);
	List<Solution> getCompileSolutions();
	
	void setRuntimeSolutions( List<Solution> solutions);
	List<Solution> getRuntimeSolutions();
	
}
