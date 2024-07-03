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
package com.braintribe.devrock.model.devenv;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * model for various data 
 * @author pit
 *
 */
public interface DevEnvConfiguration  extends GenericEntity {
		
	final EntityType<DevEnvConfiguration> T = EntityTypes.T(DevEnvConfiguration.class);


	/**
	 * @return - true if a compiled repository configuration should be stored as YAML
	 */
	boolean getDumpRepositoryConfiguration();
	void setDumpRepositoryConfiguration(boolean  dumpRepositoryConfiguration);
	
	/**
	 * @return - the fully qualified name of the directory to put the YAML files of the repository configuration in 
	 */
	String getRepositoryDumpLocation();
	void setRepositoryDumpLocation(String  repositoryDumpLocation);
	
	
	/**
	 * @return - true if a resolution should be stored as YAML
	 */
	boolean getDumpResolution();
	void setDumpResolution(boolean  dumpResolution);
	
	/**
	 * @return - the fully qualified name of the directory to put the YAML files of the resolutin configuration in
	 */
	String getResolutionDumpLocation();
	void setResolutionDumpLocation(String  ResolutionDumpLocation);
	
	
	/**
	 * @return - true if any issue (that would lead to a repo being offline other than declared) is to lead to a failed configuration
	 */
	boolean getTreatProbingIssuesAsFailure();
	void setTreatProbingIssuesAsFailure(boolean  treatProbingIssuesAsFailure);
	

}
