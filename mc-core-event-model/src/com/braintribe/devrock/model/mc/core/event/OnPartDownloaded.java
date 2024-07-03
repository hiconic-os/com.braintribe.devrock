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
import com.braintribe.model.resource.Resource;
import com.braintribe.model.time.TimeSpan;

public interface OnPartDownloaded extends GenericEntity {
	EntityType<OnPartDownloaded> T = EntityTypes.T(OnPartDownloaded.class);
	
	static String part = "part";
	static String repositoryOrigin = "repositoryOrigin";
	static String resource = "resource";
	static String elapsedTime = "elapsedTime";
	
	static String downloadSize = "downloadSize";
	CompiledPartIdentification getPart();
	void setPart(CompiledPartIdentification part);
	
	String getRepositoryOrigin();
	void setRepositoryOrigin(String repositoryOrigin);
	
	Resource getResource();
	void setResource(Resource resource);
	
	TimeSpan getElapsedTime();
	void setElapsedTime(TimeSpan elapsedTime);
	
	long getDownloadSize();
	void setDownloadSize(long downloadSize);
	
}
