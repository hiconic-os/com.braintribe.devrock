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
package com.braintribe.test.multi.framework.fake.direct;

import java.io.File;
import java.io.IOException;

import com.braintribe.model.ravenhurst.Artifact;
import com.braintribe.utils.IOTools;

public abstract class AbstractDirectClientBase {
	protected String [] artifacts;
	protected String url;
	protected String [] expansive_extensions = new String [] {".jar", ".pom", "-sources.jar", "-javadoc.jar",};
	protected String [] restricted_extensions = new String [] {".jar", ".pom",};
	protected boolean expansive;
	private String key;
	private File home = new File( "res/walk/pom");
	
	protected AbstractDirectClientBase(String key, boolean expansive, String... artifacts) {		
		this.key = key;
		this.expansive = expansive;
		this.artifacts = artifacts;		
	}
		
	protected Artifact buildArtifact( String value) {
		int gp = value.indexOf(':');
		int vs = value.indexOf( '#');
		Artifact artifact = Artifact.T.create();
		artifact.setGroupId( value.substring(0, gp));
		artifact.setArtifactId( value.substring(gp+1, vs));
		artifact.setVersion( value.substring(vs+1));
		return artifact;
	}
	
	protected String buildFilenamePrefix( String value) {
		int gp = value.indexOf(':');
		int vs = value.indexOf( '#');	
		return value.substring(gp+1, vs) + "-" + value.substring(vs+1);
	}
	
	protected String getKey() {
		return key;
	}
	
	protected String extractPom(String source) throws IOException {
		String name = source.substring( source.lastIndexOf('/')+1);
		File pomFile = new File( home, name);
		return IOTools.slurp(pomFile, "UTF-8");
	}
	
}
