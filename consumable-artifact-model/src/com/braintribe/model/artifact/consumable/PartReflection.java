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
package com.braintribe.model.artifact.consumable;

import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * extends the essential {@link PartIdentification} with the origin (the repo it comes from)
 * 
 * @author pit
 *
 */
public interface PartReflection extends PartIdentification {
	
	EntityType<PartReflection> T = EntityTypes.T(PartReflection.class);
	
	String repositoryOrigin = "repositoryOrigin";

	/**
	 * @return - the repository id of the repository where the part came from
	 */
	String getRepositoryOrigin();
	void setRepositoryOrigin(String repositoryOrigin);
	

	
	/**
	 * @param compiledPartIdentification - a {@link CompiledPartIdentification}
	 * @param repositoryId - the name (id) of the repository 
	 * @return - a created {@link PartReflection} entity
	 */
	static PartReflection from( CompiledPartIdentification compiledPartIdentification, String repositoryId) {
		return create( compiledPartIdentification.getClassifier(), compiledPartIdentification.getType(), repositoryId);
	}
	
	/** 
	 * @param classifier - the classifier  
	 * @param type - the type 
	 * @param repositoryId - the name (id) of the repository 
	 * @return - a created {@link PartReflection} entity
	 */
	static PartReflection create(String classifier, String type, String repositoryId) {
		PartReflection pr = PartReflection.T.create();
		pr.setClassifier( classifier);
		pr.setType( type);
		pr.setRepositoryOrigin(repositoryId);
		return pr;
	}
	
	
	/**
	 * returns a formatted string
	 */
	default String asString() {
		StringBuilder builder = new StringBuilder();
		builder.append(PartIdentification.asString(this));
		String origin = getRepositoryOrigin();
		if (origin != null) {
			builder.append(" -> ");
			builder.append(origin);
		}
		
		return builder.toString();
	}

}
