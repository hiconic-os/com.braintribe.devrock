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
import java.util.UUID;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomReaderException;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.testing.category.KnownIssue;

@Category(KnownIssue.class)
public class PomPropertyEnsurerTest extends AbstractPomReaderLab{

	@BeforeClass
	public static void runBefore  () {
		runbefore();		
	}
	
	private Solution testArtifactEnsurer( File testPomFile) {
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
			
	
	private String drillDownToStringValue(GenericEntity solution, String path) {
		String [] keys = path.split( "/");
		int i = 0;
		GenericEntity genE = solution;
		do {
			String key = keys[i];
			Property property = genE.entityType().findProperty(key);
			if (property == null) {
				return null;
			}
			Object obj = property.get(genE);
			if (obj == null) {
				return null;
			}
			if (obj instanceof String) {
				return (String) obj;
			}
			if (obj instanceof GenericEntity == false) {
				throw new IllegalStateException("unknown type");
			}
			if (obj instanceof Version) {
				return VersionProcessor.toString( (Version) obj);
			}
			if (obj instanceof VersionRange) {
				return VersionRangeProcessor.toString( (VersionRange) obj);
			}
			if (i == keys.length - 1 ) {
				throw new IllegalStateException("running out of keys, last [" + key + "]");
			}
			
			genE = (GenericEntity) obj;
			i++;			
			
		} while (true);
		
	}
	
	private void validate( GenericEntity e, String path, String expected) {
		String value = drillDownToStringValue(e, path);
		Assert.assertTrue( "property [" + path + "], expected [" + expected + "], found [" + value + "]", expected.equalsIgnoreCase(value));				
	}
	
	@Test
	@Category(KnownIssue.class)
	public void test() {
		File pom = new File( contents, "testArtifactPropertyResolvingPom.xml");
		Solution solution = testArtifactEnsurer( pom);
		
		validate( solution, "groupId", "org.kie");	
		validate( solution, "artifactId", "TestTerminal");
		validate( solution, "version", "1.0");
		validate( solution, "packaging", "pom");
		
		/*
		values.put("parent/artifactId", "kie-api-parent");
		values.put( "dependencies/dependency/classifier", "classifier");
		values.put( "dependencies/dependency/type", "type");
		*/
		
		Dependency parent = solution.getParent();
		validate( parent, "artifactId", "kie-api-parent");
		validate( parent, "groupId", "org.kie");
		validate( parent, "versionRange", "6.4.0.FINAL");
		
		
		Dependency dependency = solution.getDependencies().get(0);
		validate( dependency, "groupId", "org.kie");
		validate( dependency, "artifactId", "TestDependency");
		validate( dependency, "versionRange", "1.0");
		validate( dependency, "type", "type");
		//validate( dependency, "classifier", "windows-x86_64");
		validate( dependency, "classifier", "${os.detected.classifier}");
		validate( dependency, "scope", "scope");
		
		Dependency manangedDependency = solution.getManagedDependencies().get(0);
		validate( manangedDependency, "groupId", "org.kie");
		validate( manangedDependency, "artifactId", "TestDependency");
		validate( manangedDependency, "versionRange", "1.0");
		validate( manangedDependency, "type", "type");
		validate( dependency, "classifier", "${os.detected.classifier}");
		validate( dependency, "scope", "scope");
		
	}
}
