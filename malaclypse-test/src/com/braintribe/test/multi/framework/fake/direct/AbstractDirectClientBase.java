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
