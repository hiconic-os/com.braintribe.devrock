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
import java.text.SimpleDateFormat;

import com.braintribe.model.artifact.Identification;
import com.braintribe.model.ravenhurst.Artifact;
import com.braintribe.test.multi.framework.SnapshotTuple;
import com.braintribe.utils.IOTools;

public class AbstractDirectSnapshotClientBase {
	protected SnapshotTuple [] tuples;
	protected String url;
	protected String [] expansive_extensions = new String [] {".jar", ".pom", "-sources.jar", "-javadoc.jar",};
	protected String [] restricted_extensions = new String [] {".jar", ".pom",};
	protected boolean expansive;
	protected String key;
	protected SimpleDateFormat format = new SimpleDateFormat( "yyyyMMdd.HHmmss");
	private File home = new File( "res/walk/pom");

	public AbstractDirectSnapshotClientBase(String key, boolean expansive, SnapshotTuple ...snapshotTuples) {
		this.tuples = snapshotTuples;
		this.key = key;
		this.expansive = expansive;
	}
	
	protected boolean match( Identification artifact, String condensed) {
		int p = condensed.indexOf( ':');
		int q = condensed.indexOf('#');
		
		String grp = condensed.substring(0, p);
		String art = condensed.substring( p+1, q);		
		
		if (
				artifact.getGroupId().equalsIgnoreCase( grp) &&
				artifact.getArtifactId().equalsIgnoreCase( art)		
			) {
			return true;
		}
		return false;
	}
	
	protected boolean match( Artifact artifact, String condensed) {
		int p = condensed.indexOf( ':');
		int q = condensed.indexOf('#');
		
		String grp = condensed.substring(0, p);
		String art = condensed.substring( p+1, q);
		String vrs = condensed.substring( q+1);
		
		if (
				artifact.getGroupId().equalsIgnoreCase( grp) &&
				artifact.getArtifactId().equalsIgnoreCase( art) &&
				artifact.getVersion().equalsIgnoreCase( vrs)
			) {
			return true;
		}
		return false;
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
	
	protected String buildFilenamePrefix( SnapshotTuple tuple, int i) {
		String value = tuple.getArtifact();
		int gp = value.indexOf(':');
		int vs = value.indexOf( '#');	
		return value.substring(gp+1, vs) + "-" + value.substring(vs+1) + "-" + format.format( tuple.getTimestamps()[i] + "-" + tuple.getBuilds()[i]);
	}
	
	protected String getKey() {
		return key;
	}
	
	protected SnapshotTuple identifyTuple( SnapshotTuple [] tuples, String grp, String art, String vrs){
		for (SnapshotTuple tuple : tuples) {
			String artifactAsString = tuple.getArtifact();
			Artifact artifact = buildArtifact(artifactAsString);
			if (
					artifact.getGroupId().equalsIgnoreCase(grp) &&
					artifact.getArtifactId().equalsIgnoreCase(art) &&
					artifact.getVersion().equalsIgnoreCase(vrs)
				) {		
				return tuple;
			}
		}
		return null;
	}
	
	protected String extractPom(String source) throws IOException {
		String name = source.substring( source.lastIndexOf('/')+1);
		for (SnapshotTuple tuple : tuples) {
			if (name.matches( tuple.getPomMatch())) {
				File pomFile = new File( home, tuple.getPomFile());
				return IOTools.slurp(pomFile, "UTF-8");		
			}
		}
		return null;
	}
}
