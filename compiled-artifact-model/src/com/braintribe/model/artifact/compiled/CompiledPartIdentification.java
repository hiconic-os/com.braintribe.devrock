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

import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.version.Version;

public interface CompiledPartIdentification extends CompiledArtifactIdentification, PartIdentification {
	
	
	EntityType<CompiledPartIdentification> T = EntityTypes.T(CompiledPartIdentification.class);
	
	
	/**
	 * @param cai - the {@link CompiledArtifactIdentification}
	 * @return - a {@link CompiledPartIdentification} of type "jar" and no classifier
	 */
	static CompiledPartIdentification from(CompiledArtifactIdentification cai) {
		CompiledPartIdentification cpi = CompiledPartIdentification.T.create();
		cpi.setGroupId( cai.getGroupId());
		cpi.setArtifactId( cai.getArtifactId());
		cpi.setVersion( cai.getVersion());
			
		cpi.setType( "jar");
		
		return cpi;
	}
	
	
	static CompiledPartIdentification from(CompiledArtifactIdentification cai, PartIdentification pi) {
		CompiledPartIdentification cpi = CompiledPartIdentification.T.create();
		cpi.setGroupId( cai.getGroupId());
		cpi.setArtifactId( cai.getArtifactId());
		cpi.setVersion( cai.getVersion());
		
		cpi.setClassifier( pi.getClassifier());
		cpi.setType( pi.getType());
		
		return cpi;
	}
	
	static CompiledPartIdentification create(String groupId, String artifactId, String version, String classifier, String type) {
		CompiledPartIdentification cpi = CompiledPartIdentification.T.create();
		cpi.setGroupId( groupId);
		cpi.setArtifactId( artifactId);
		cpi.setVersion( Version.parse(version));
		if (classifier != null)
		cpi.setClassifier( classifier);
		cpi.setType( type);		
		return cpi;
	}
	
	static CompiledPartIdentification create(String groupId, String artifactId, String version, String type) {
		return create( groupId, artifactId, version, null, type);
	}
	
	
	@Override
	default String asString() {	
		return CompiledArtifactIdentification.super.asString() + "/" + PartIdentification.super.asString();
	}
	
	default String asFilename() {
		StringBuilder sb = new StringBuilder();
		sb.append( this.getArtifactId());
		sb.append( '-');
		sb.append( this.getVersion().asString());
		String extractedClassifier = this.getClassifier();
		if (extractedClassifier != null) {
			sb.append( '-');
			sb.append( extractedClassifier);
		}
		String extractedType = this.getType();
		if (extractedType != null) { 
			sb.append( '.');
			sb.append( extractedType);
		}
		
		return sb.toString();
	}
	
	static CompiledPartIdentification fromFile( CompiledArtifactIdentification cai, String fileName) {
		String prefix = cai.getArtifactId() + "-" + cai.getVersion().asString();
		if (!fileName.startsWith( prefix)) {
			return null;
		}
		int l = prefix.length();
		int p = fileName.indexOf( prefix);
		String remainder = fileName.substring( p + l);
		
		if (remainder.startsWith("-")) {
			remainder = remainder.substring( 1);
		}
		remainder = remainder.replace( ".", ":");
		CompiledPartIdentification result = from( cai, PartIdentification.parse(remainder));
		return result;				
	}

		
		
}
