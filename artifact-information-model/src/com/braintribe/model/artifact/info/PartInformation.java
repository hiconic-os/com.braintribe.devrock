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
package com.braintribe.model.artifact.info;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * represents a part of an artifact, i.e. a single file 
 * 
 * @author pit
 *
 */
public interface PartInformation extends GenericEntity {
	
	final EntityType<PartInformation> T = EntityTypes.T(PartInformation.class);

	/**
	 * classifier of the part 
	 * @return
	 */
	String getClassifier();
	/**
	 * classifier of the part 
	 * @param classifier
	 */
	void setClassifier( String classifier);
		
	/**
	 * type (extension) of the part
	 * @return
	 */
	String getType();
	
	/**
	 * type (extension) of the part
	 * @param partType
	 */
	void setType( String partType);
	
	/**
	 * download URL (or local file system URL)
	 * @return
	 */
	String getUrl();
	/**
	 * download URL (or local file system URL)	 
	 * @param url
	 */
	void setUrl( String url);
}
