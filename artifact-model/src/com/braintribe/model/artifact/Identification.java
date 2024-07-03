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
package com.braintribe.model.artifact;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


/**
 * this is the base of the artifact family
 * @author pit
 *
 */

public interface Identification extends StandardIdentifiable {
	
	final EntityType<Identification> T = EntityTypes.T(Identification.class);
	
	public String getGroupId();
	public void setGroupId(String group);
	
	public String getArtifactId();
	public void setArtifactId(String id);
	
	public String getRevision();
	public void setRevision( String revision);
	
	public String getClassifier();
	public void setClassifier( String classifier);
}
