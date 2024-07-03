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
package com.braintribe.artifacts.test.maven.settings.evaluation;

import java.io.File;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.model.maven.settings.Mirror;

public class MirrorResolvingLab extends AbstractMavenSettingsLab{
	private static File contents = new File( "res/mirrorLab/contents");

	private static File settings = new File( contents, "settings.multimirror.xml");
	private static File localRepository = new File ( contents, "repo");
	private static MavenSettingsReader reader;
	private Triple [] triples;
	
	private class Triple {
		public String mirror;
		public String repo;
		public String url;
		
		public Triple( String mirror, String repo, String url) {
			this.mirror = mirror;
			this.repo = repo;
			this.url = url;
		}
	}
	
	{
		triples = new Triple [] { 
									new Triple("archiva.mirror", "archiva", "http://archiva.bt.com/repository/standalone"),
									new Triple("tribefire.mirror", "tribefire", "https://artifactory.example.com/tribefire-repository-2.0-snapshot"),
									new Triple("empty.mirror", "empty", "file:///home/mla/Documents/Dev/firectrl/repositories/empty"),
								};  
	}
	

	@BeforeClass
	public static void before() {
		before(settings, localRepository);
		reader = getReader();
	}


	private String testMirrorAssignement( Triple triple) {
		Mirror mirror =  reader.getMirror(triple.repo, triple.url);
		if (mirror == null) {
			return null;
		}
		return mirror.getId();
	}
	
	@Test
	public void test() {
		for (Triple triple : triples) {
			String mirror = testMirrorAssignement(triple);
			Assert.assertTrue( "mirror of [" + triple.repo + "] is not [" + triple.mirror + "] but [" + mirror + "]",  triple.mirror.equalsIgnoreCase( mirror));
		}
		
	}

}
