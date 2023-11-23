// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
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
