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
