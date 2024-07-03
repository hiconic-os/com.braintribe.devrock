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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * the basic identification of an artifact
 * @author pit/dirk
 *
 */
public interface ArtifactIdentification extends GenericEntity{

	
	EntityType<ArtifactIdentification> T = EntityTypes.T(ArtifactIdentification.class);
	static final String groupId = "groupId";
	static final String artifactId = "artifactId";

	/**
	 * @return - the group id
	 */
	String getGroupId();
	void setGroupId( String groupId);
	
	/**
	 * @return - the artifact id
	 */
	String getArtifactId();
	void setArtifactId( String artifactId);
	
	@Override
	default String stringify() {
		return asString();
	}
	
	/**
	 * @return - a string representation {@code <groupId>:<artifactId>#<version>}
	 */
	default String asString() {
		return asString(this);
	}

	/**
	 * @return - a string representation {@code <groupId>:<artifactId>#<version>}
	 */
	static String asString(ArtifactIdentification ai) {
		StringBuilder sb = new StringBuilder();
		sb.append( ai.getGroupId());
		sb.append( ":");
		sb.append( ai.getArtifactId());
		return sb.toString();
	}
	
	/**
	 * parses a condensed *un-versioned* artifact, i.e. group and artifact only 
	 * @param string - the condensed string,  {@code <groupId>:<artifactId>}
	 * @return - a fresh {@link ArtifactIdentification}
	 */
	static ArtifactIdentification parse(String string) {
		int pA = string.lastIndexOf( ':');		
		if (pA < 0) {
			throw new IllegalArgumentException( " group/artifact are required");
		}				
		return create( string.substring(0, pA), string.substring( pA+1));						
	}
	
	/**
	 * creates a {@link ArtifactIdentification} from the passed groupId and artifactId
	 * @param groupId - the groupId
	 * @param artifactId - the artifactId
	 * @return - a fresh {@link ArtifactIdentification}
	 */
	static ArtifactIdentification create( String groupId, String artifactId) {
		ArtifactIdentification artifactIdentification = ArtifactIdentification.T.create();
		artifactIdentification.setGroupId( groupId);
		artifactIdentification.setArtifactId( artifactId);		
				
		return artifactIdentification;
	}
	
	static ArtifactIdentification from( ArtifactIdentification artifactIdentification) {
		return create( artifactIdentification.getGroupId(), artifactIdentification.getArtifactId());		
	}
	
	default int compareTo(ArtifactIdentification o) {
		int retval = this.getGroupId().compareTo( o.getGroupId());
		if (retval != 0)
			return retval;
		return this.getArtifactId().compareToIgnoreCase( o.getArtifactId());		
	}
	
	
}
