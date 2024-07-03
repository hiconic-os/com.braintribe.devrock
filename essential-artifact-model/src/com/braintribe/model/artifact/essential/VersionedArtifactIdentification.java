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
package com.braintribe.model.artifact.essential;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * an {@link ArtifactIdentification} that has a version 
 * @author pit/dirk
 *
 */
public interface VersionedArtifactIdentification extends ArtifactIdentification {

	
	EntityType<VersionedArtifactIdentification> T = EntityTypes.T(VersionedArtifactIdentification.class);
	String version = "version";

	/**
	 * @return - the version (or range)
	 */
	String getVersion();
	void setVersion( String version);
	
	/**
	 * @return - a string representation {@code <groupId>:<artifactId>#<version>}
	 */
	default String asString() {
		StringBuilder sb = new StringBuilder();
		String groupId = this.getGroupId();
		String artifactId = this.getArtifactId();
		sb.append(groupId != null? groupId: "<n/a>");
		sb.append( ":");
		sb.append(artifactId != null? artifactId: "<n/a>");
		sb.append( "#");
		
		String version = this.getVersion();
		
		if (version != null)
			sb.append( version);
		else
			sb.append("<n/a>");
		
		return sb.toString();
	}
	
	/**
	 * parses a condensed *versioned* artifact, i.e. group and artifact and a string version 
	 * @param string - the condensed string,  {@code <groupId>:<artifactId>#<version>}
	 * @return - a fresh {@link VersionedArtifactIdentification}
	 */
	static VersionedArtifactIdentification parse(String string) {
		if (string == null) {
			throw new IllegalArgumentException( "expression may not be null");
		}
		int pH = string.lastIndexOf( '#');
		if (pH < 0) {
			throw new IllegalArgumentException( "an version is required in [" + string + "]");
		}
		int pA = string.lastIndexOf( ':', pH);
		
		if (pA < 0) {
			throw new IllegalArgumentException( " group/artifact are required in [" + string + "]");
		}		
		return create( string.substring(0, pA), string.substring( pA+1, pH), string.substring( pH+1));		
	}
	
	/**
	 * create a {@link VersionedArtifactIdentification} by giving the three identifying parts
	 * @param groupId - the group
	 * @param artifactId - the id 
	 * @param version -  the version
	 * @return - a newly created {@link VersionedArtifactIdentification}
	 */
	static VersionedArtifactIdentification create( String groupId, String artifactId, String version) {
		VersionedArtifactIdentification artifactIdentification = VersionedArtifactIdentification.T.create();
		artifactIdentification.setGroupId( groupId);
		artifactIdentification.setArtifactId( artifactId);
		artifactIdentification.setVersion( version );		
		return artifactIdentification;		
	}
		
}
