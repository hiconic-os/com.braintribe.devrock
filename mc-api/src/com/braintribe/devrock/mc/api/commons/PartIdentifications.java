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
package com.braintribe.devrock.mc.api.commons;

import com.braintribe.model.artifact.essential.PartIdentification;

/**
 * lill' helper to define the most commonly used {@link PartIdentification}
 * 
 * @author pit/dirk
 *
 */
public interface PartIdentifications {

	PartIdentification pom = PartIdentification.create( "pom");
	
	// this part serves the special purpose to mark an artifact as completely published
	PartIdentification publishComplete = PartIdentification.create( "publish-complete");
	
	String pomPartKey = pom.asString();
	
	PartIdentification jar = PartIdentification.create( "jar");
	PartIdentification sources_jar = PartIdentification.create( "sources", "jar");
	PartIdentification javadoc_jar = PartIdentification.create( "javadoc", "jar");
	PartIdentification classes_jar = PartIdentification.create( "classes", "jar");
	PartIdentification md5 = PartIdentification.create( "md5");
	PartIdentification sha1 = PartIdentification.create( "sha1");
	PartIdentification asc = PartIdentification.create( "asc");
}
