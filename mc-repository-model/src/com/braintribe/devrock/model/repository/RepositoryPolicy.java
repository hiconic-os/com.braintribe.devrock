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
package com.braintribe.devrock.model.repository;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * declares settings (enablement, update policy enum and parameters (RH URL, intervall expression)<br/> 
 * 
 * {@link RepositoryPolicy#getUpdatePolicyParameter()} : contains either the RH URL or the interval specification, 
 * see Maven documentation  ([interval:]XXX, XXX in minutes)
 * 
 * @author pit
 *
 */
public interface RepositoryPolicy extends GenericEntity {
	
	final EntityType<RepositoryPolicy> T = EntityTypes.T(RepositoryPolicy.class);
	
	// default ignore.. 
	/**
	 * @return - the {@link ChecksumPolicy} set (may be null, defaulting to 'ignore')
	 */
	ChecksumPolicy getCheckSumPolicy();
	/**
	 * @param checkSumPolicy - the {@link ChecksumPolicy} (may be not-set/null, defaulting to 'ignore')
	 */
	void setCheckSumPolicy( ChecksumPolicy checkSumPolicy);

	// default : never ?
	/**
	 * @return - the {@link UpdatePolicy} for the {@link RepositoryPolicy}.
	 * May be null, defaults to 'never'
	 */
	UpdatePolicy getUpdatePolicy();
	/**
	 * @param updatePolicy- the {@link UpdatePolicy} for the {@link RepositoryPolicy}.
	 * May be null, defaults to 'never'
	 */
	void setUpdatePolicy( UpdatePolicy updatePolicy);
	
	/**
	 * if the update policy requires parameters {@link UpdatePolicy#never} or {@link UpdatePolicy#interval}, this must contain it
	 * @return - the required parameter for the chosen {@link UpdatePolicy}  
	 */
	String getUpdatePolicyParameter();
	/**
	 * if the update policy requires parameters {@link UpdatePolicy#never} or {@link UpdatePolicy#interval}, this must contain it
	 * @param updatePolicyParameter - the required parameter for the chosen {@link UpdatePolicy}
	 */
	void setUpdatePolicyParameter(String updatePolicyParameter);
	
	/**
	 * @return - whether to enable this {@link RepositoryPolicy}
	 */
	boolean getEnabled();
	/**
	 * @param enabled - whether to enable this {@link RepositoryPolicy}
	 */
	void setEnabled( boolean enabled);
	
	

}
