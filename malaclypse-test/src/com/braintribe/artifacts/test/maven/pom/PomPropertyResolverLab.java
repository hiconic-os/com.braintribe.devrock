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
package com.braintribe.artifacts.test.maven.pom;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;

public class PomPropertyResolverLab extends AbstractPomReaderLab{
	
	
	@BeforeClass
	public static void runBefore  () {
		runbefore();		
	}

	private void testPropertyResolvingViaSettings(File testPomFile) {	
		ArtifactPomReader reader = pomExpertFactory.getReader();
		Solution solution;
		try {
			solution = reader.readPom( UUID.randomUUID().toString(), testPomFile);
		} catch (PomReaderException e) {
			Assert.fail("exception thrown :" + e.getMessage());
			return;
		}
		
		Map<String, Map<String, String>> properties;
		try {
			properties = mavenSettingsReader.getPropertiesOfActiveProfiles();
		} catch (RepresentationException e) {
			Assert.fail("exception thrown :" + e.getMessage());
			return;
		}
		Map<String, String> propertiesOfBraintribe = properties.get("braintribe");
		String stGrp = propertiesOfBraintribe.get( "testPomGroup");
		String grp = solution.getGroupId();
		if (!grp.equalsIgnoreCase(stGrp)) {
			Assert.fail( "group [" + grp + "] found, expected [" + stGrp + "]");
			return;
		}
		String stArt = propertiesOfBraintribe.get("testPomArtifact");
		String art = solution.getArtifactId();
		if (!art.equalsIgnoreCase(stArt)) {
			Assert.fail("artifact [" + art + "] found, expected [" + stArt + "]");
			return;
		}
		String stVrs = propertiesOfBraintribe.get("testPomVersion");		
		String vrs = VersionProcessor.toString( solution.getVersion());
		
		if (!vrs.equalsIgnoreCase( stVrs)) {
			Assert.fail("artifact [" + vrs + "] found, expected [" + stVrs + "]");
			return;
		}	
	}
	
	@Test
	public void testStandardResolvingViaSettings() {
		testPropertyResolvingViaSettings( new File( contents, "testPropertyResolvingPom.xml"));
	}
	
	@Test
	public void testReflectionResolvingViaSettings() {
		testPropertyResolvingViaSettings( new File( contents, "testPropertyResolvingReflectionPom.xml"));
	}

}
