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
