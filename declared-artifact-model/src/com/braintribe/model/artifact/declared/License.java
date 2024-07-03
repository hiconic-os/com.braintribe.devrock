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
