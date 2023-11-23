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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.model.artifact.Distribution;
import com.braintribe.model.artifact.License;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;

public class LicenseLab extends AbstractPomReaderLab {

	@BeforeClass
	public static void runBefore  () {
		runbefore();		
	}
	
	private Solution read( File testPomFile) {
		ArtifactPomReader reader = pomExpertFactory.getReader();
		Solution solution;
		try {
			solution = reader.readPom( UUID.randomUUID().toString(), testPomFile);
			return solution;
		} catch (PomReaderException e) {
			Assert.fail("exception thrown :" + e.getMessage());
			return null;
		}
	}
	
	private void testLicense(String pom, Map<String, License> expectedLicenses) {
		File testPomFile = new File( contents, pom);
		Solution solution = read( testPomFile);
		Set<License> licenses = solution.getLicenses();
		if (licenses == null || licenses.size() == 0) {
			Assert.fail( "no licenses found");
			return;
		}
		
		for (License license : licenses) {
			String name = license.getName();
			License expected = expectedLicenses.get( name);
			if (expected == null) {
				Assert.fail("license [" + name + "] is not expected");
				continue;
			}
			Assert.assertTrue("found url [" + license.getUrl() + "], expected [" + expected.getUrl() + "]", expected.getUrl().equalsIgnoreCase(license.getUrl()));
			Assert.assertTrue("found distribution [" + license.getDistribution() + "], expected [" + expected.getDistribution() + "]", expected.getDistribution() == license.getDistribution());
		}
	
		System.out.println("group [" + solution.getGroupId() + "]");
		System.out.println("artifact [" + solution.getArtifactId() + "]");
		
		System.out.println("version [" + VersionProcessor.toString( solution.getVersion()) + "]");
	}
	
	@Test
	public void testSingleLicense() {
		License license = License.T.create();
		license.setName( "Braintribe License");
		license.setUrl( "http://license.braintribe.com");
		license.setDistribution( Distribution.manual);
		Map<String, License> expected = new HashMap<String, License>();
		expected.put( license.getName(), license);
		testLicense( "singleLicense.xml", expected);
	}
	
	

}
