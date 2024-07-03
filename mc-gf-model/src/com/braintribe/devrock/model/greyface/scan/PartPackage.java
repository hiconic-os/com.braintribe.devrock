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
package com.braintribe.devrock.model.greyface.scan;

import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * represents a part (potential or real, depends on file or http URL) 
 *  
 * 
 * @author pit
 *
 */
public interface PartPackage extends CompiledPartIdentification {
	
	EntityType<PartPackage> T = EntityTypes.T(PartPackage.class);

	/**
	 * @return - either a http or file URL
	 */
	String getOriginUrl();
	void setOriginUrl(String value);	
}
