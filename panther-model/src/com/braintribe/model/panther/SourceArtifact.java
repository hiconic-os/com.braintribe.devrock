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
package com.braintribe.model.panther;

import java.util.Set;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


@SelectiveInformation("${artifactId}#${version} (${groupId})")
public interface SourceArtifact extends StandardIdentifiable {
	
	final EntityType<SourceArtifact> T = EntityTypes.T(SourceArtifact.class);

	public final static String repository = "repository";
	public final static String groupId = "groupId";
	public final static String artifactId = "artifactId";
	public final static String version = "version";
	public final static String path = "path";
	public final static String grouped = "grouped";
	public final static String natures = "natures";

	
	
	void setRepository(SourceRepository repository);
	SourceRepository getRepository();
	
	void setGroupId(String groupId);
	String getGroupId();
	
	void setArtifactId(String artifactId);
	String getArtifactId();

	void setVersion(String version);
	String getVersion();
	
	void setPath(String path);
	String getPath();
	
	void setGrouped(boolean grouped);
	boolean getGrouped();
	
	void setNatures(Set<ProjectNature> natures);
	Set<ProjectNature> getNatures();
}
