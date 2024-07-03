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
package com.braintribe.devrock.model.mc.core.event;

import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface OnPartDownloading extends GenericEntity {
	EntityType<OnPartDownloading> T = EntityTypes.T(OnPartDownloading.class);
	
	static String part = "part";
	static String repositoryOrigin = "repositoryOrigin";
	static String resource = "resource";

	CompiledPartIdentification getPart();
	void setPart(CompiledPartIdentification part);
	
	String getRepositoryOrigin();
	void setRepositoryOrigin(String repositoryOrigin);
	
	int getDataAmount();
	void setDataAmount(int dataAmount);
	
	int getTotalDataAmount();
	void setTotalDataAmount(int totalDataAmount);

}
