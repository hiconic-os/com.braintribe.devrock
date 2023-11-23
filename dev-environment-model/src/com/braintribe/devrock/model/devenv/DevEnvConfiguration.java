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
