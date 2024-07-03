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
package com.braintribe.model.artifact.compiled;

import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.version.Version;

/**
 * a {@link CompiledArtifactIdentification} represents a fully qualified artifact
 * @author pit/dirk
 *
 */
public interface CompiledArtifactIdentification extends ArtifactIdentification, Comparable<CompiledArtifactIdentification>{
	
	EntityType<CompiledArtifactIdentification> T = EntityTypes.T(CompiledArtifactIdentification.class);
	String version = "version";

	/**
	 * @return - the {@link Version}
	 */
	Version getVersion();
	void setVersion( Version version);

 
	/**
	 * @return - a string representation {@code <groupId>:<artifactId>#<version>}
	 */
	@Override
	default String asString() {
		StringBuilder sb = new StringBuilder();
		String groupId = this.getGroupId();
		String artifactId = this.getArtifactId();
		sb.append(groupId != null? groupId: "<n/a>");
		sb.append( ":");
		sb.append(artifactId != null? artifactId: "<n/a>");
		sb.append( "#");
		
		Version version = this.getVersion();
		
		if (version != null)
			sb.append( version.asString());
		else
			sb.append("<n/a>");
		
		return sb.toString();
	}
	
	@Override
	default int compareTo(CompiledArtifactIdentification o) {
		int retval = this.getGroupId().compareTo( o.getGroupId());
		if (retval != 0)
			return retval;
		retval = this.getArtifactId().compareToIgnoreCase( o.getArtifactId());
		if (retval != 0)
			return retval;
		return this.getVersion().compareTo(o.getVersion());		
	}
	
	/**
	 * parses a condensed *compiled* artifact, i.e. group and artifact and a full version 
	 * @param string - the condensed string,  {@code <groupId>:<artifactId>#<version>}
	 * @return - a fresh {@link CompiledArtifactIdentification}
	 */
	static CompiledArtifactIdentification parse(String string) {
		
		int pH = string.lastIndexOf( '#');
		if (pH < 0) {
			throw new IllegalArgumentException( "an version is required");
		}
		int pA = string.lastIndexOf( ':', pH);
		
		if (pA < 0) {
			throw new IllegalArgumentException( " group/artifact are required");
		}						
		return create( string.substring(0, pA), string.substring( pA+1, pH), string.substring( pH+1));
	}
	
	static CompiledArtifactIdentification create( String groupId, String artifactId, String version) {
		CompiledArtifactIdentification artifactIdentification = CompiledArtifactIdentification.T.create();
		artifactIdentification.setGroupId( groupId);
		artifactIdentification.setArtifactId( artifactId);
		artifactIdentification.setVersion( Version.parse( version));			
		return artifactIdentification;
	}
	
	static CompiledArtifactIdentification from(CompiledArtifactIdentification cai) {
		return from(cai, cai.getVersion());
	}
	
	static CompiledArtifactIdentification from( ArtifactIdentification artifactIdentification, Version version) {
		CompiledArtifactIdentification cai = CompiledArtifactIdentification.T.create();
		cai.setGroupId( artifactIdentification.getGroupId());
		cai.setArtifactId( artifactIdentification.getArtifactId());
		cai.setVersion( version);
		return cai;
	}
	
	static CompiledArtifactIdentification from( VersionedArtifactIdentification artifactIdentification) {
		return from( artifactIdentification, Version.parse( artifactIdentification.getVersion()));
	}
	
	default boolean isSnapshot() {
		Version actualVersion = getVersion();
		if (actualVersion == null) {
			throw new IllegalStateException("version of ["+ getVersion().asString() + "] may not be null at this point");
		}
		return actualVersion.isSnapshot();
	}
	default boolean isRelease() {		
		return !isSnapshot();
	}
	
	
}
