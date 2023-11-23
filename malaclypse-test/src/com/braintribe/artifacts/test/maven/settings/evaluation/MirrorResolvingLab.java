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
