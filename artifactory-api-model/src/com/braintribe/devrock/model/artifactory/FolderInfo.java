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
package com.braintribe.devrock.model.artifactory;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * reverse engineered from artifact JSON response : part-availability as reflected by artifactory<br/>
 * @author pit/dirk
 *
 */
public interface FolderInfo extends GenericEntity{	
	EntityType<FolderInfo> T = EntityTypes.T(FolderInfo.class);
	String repo = "repo";
	String path = "path";
	String created = "created";
	String createdBy = "createdBy";
	String lastModified = "lastModified";
	String modifiedBy = "modifiedBy";
	String lastUpdated = "lastUpdated";
	String uri = "uri";
	String children = "children";

	String getRepo();
	void setRepo(String value);
	
	String getPath();
	void setPath(String value);

	String getCreated();
	void setCreated(String value);

	String getCreatedBy();
	void setCreatedBy(String value);

	String getLastModified();
	void setLastModified(String value);

	String getModifiedBy();
	void setModifiedBy(String value);

	String getlastUpdated();
	void setlastUpdated(String value);
	
	String getUri();
	void setUri(String value);

	List<FileItem> getChildren();
	void setChildren(List<FileItem> value);

	
}
