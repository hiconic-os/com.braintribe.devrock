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
package com.braintribe.model.panther;

import java.util.Date;
import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface Publishing extends GenericEntity {
	
	EntityType<Publishing> T = EntityTypes.T(Publishing.class);
	
	void setUser(String user);
	String getUser();
	
	void setDate(Date date);
	Date getDate();
	
	void setArtifacts(List<SourceArtifact> artifacts);
	List<SourceArtifact> getArtifacts();
	
	void setLogs(List<ArtifactPublishingLog> logs);
	List<ArtifactPublishingLog> getLogs();
	
	void setStatus(PublishingStatus status);
	PublishingStatus getStatus();
}
