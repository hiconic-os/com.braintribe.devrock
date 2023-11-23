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
package com.braintribe.model.artifact.declared;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * represents the data from the license section within the pom 
 * @author pit
 *
 */
public interface License extends GenericEntity {
	
	String name = "name";
	String url = "url";
	String distribution = "distribution";
	String comments = "comments";
	
	EntityType<License> T = EntityTypes.T(License.class);

	/**
	 * Maven says : The full legal name of the license
	 * @return - the name of the license as in the pom
	 */
	String getName();
	void setName(String name);

	/**
	 * Maven says : The official URL for the license text
	 * @return - the URL of the license as in the pom
	 */
	String getUrl();
	void setUrl( String url);
	
	/**
	 * Maven says:<br/>
	 * The primary method by which this project may be distributed.
            <dl>
              <dt>repo</dt>
              <dd>may be downloaded from the Maven repository</dd>
              <dt>manual</dt>
              <dd>user must manually download and install the dependency.</dd>
            </dl>                        
	 * @return - the description as found 
	 */
	String getDistribution();
	void setDistribution( String distribution);
		
	/**
	 * Maven says : Addendum information pertaining to this license
	 * @return - the comment tag as found in the pom
	 */
	String getComments();
	void setComments( String comments);
	
}
