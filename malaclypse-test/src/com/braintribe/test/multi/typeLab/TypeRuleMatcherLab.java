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
package com.braintribe.test.multi.typeLab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.artifacts.test.maven.pom.AbstractPomReaderLab;
import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.walk.multi.filters.TypeRuleFilter;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.VersionRange;


public class TypeRuleMatcherLab extends AbstractPomReaderLab {
	
	private static List<Dependency> dependencies;
	private static final String groupId = "com.braintribe.devrock.test.tags";
	private static final VersionRange versionRange = VersionRangeProcessor.createFromString( "1.0");
	
	@BeforeClass	
	public static void runbefore() {
		dependencies = new ArrayList<>();
		
		Dependency none = Dependency.T.create();
		none.setGroupId( groupId);
		none.setVersionRange(versionRange);
		none.setArtifactId("none");
		
		dependencies.add(none);
		
		Dependency standard = Dependency.T.create();
		standard.setGroupId( groupId);
		standard.setVersionRange(versionRange);
		standard.setArtifactId("standard");
		standard.setType("JAR");

		dependencies.add(standard);
		
		Dependency war = Dependency.T.create();
		war.setGroupId( groupId);
		war.setVersionRange(versionRange);
		war.setArtifactId("war");
		war.setType("WAR");
		
		dependencies.add(war);

		Dependency manAsset = Dependency.T.create();
		manAsset.setGroupId( groupId);
		manAsset.setVersionRange(versionRange);
		manAsset.setArtifactId("asset-man");
		manAsset.setType("MAN");
		manAsset.setClassifier( "asset");

		Dependency otherAsset = Dependency.T.create();
		otherAsset.setGroupId( groupId);
		otherAsset.setVersionRange(versionRange);
		otherAsset.setArtifactId("asset-other");
		otherAsset.setType("OTHER");
		otherAsset.setClassifier( "asset");
		
		Dependency noAssetMan = Dependency.T.create();
		noAssetMan.setGroupId( groupId);
		noAssetMan.setVersionRange(versionRange);
		noAssetMan.setArtifactId("noasset-man");
		noAssetMan.setType("MAN");
		noAssetMan.setClassifier( "noasset");
		
		Dependency zip = Dependency.T.create();
		zip.setGroupId( groupId);
		zip.setVersionRange(versionRange);
		zip.setArtifactId("zip");
		zip.setType("ZIP");
		
		dependencies.add(zip);		
	}
	
	public void test( String rule, List<String> expectedNames) {
		TypeRuleFilter matcher = new TypeRuleFilter();
		matcher.setRule(rule);
		
		List<String> unexpectedNames = new ArrayList<>();
		List<String> expectedNames2 = new ArrayList<>( expectedNames);
		List<Dependency> filtered = matcher.filterDependencies(dependencies);
		List<Dependency> processed = new ArrayList<>( filtered);
		Iterator<Dependency> iterator = processed.iterator();
		
		while (iterator.hasNext()) {
			Dependency dependency = iterator.next();
			String name = NameParser.buildName(dependency);			
			if (expectedNames.contains( name)) {
				iterator.remove();
				expectedNames2.remove( name);
			}
			else {
				unexpectedNames.add( name);
			}
		}
		if (unexpectedNames.size() == 0 && processed.size() == 0) {			
			return;
		}
		
		Assert.assertTrue( "unexpected [" + nameListToString(unexpectedNames) + "]", unexpectedNames.size() == 0);
		Assert.assertTrue( "undelivered [" + nameListToString( expectedNames2) + "]", expectedNames2.size() == 0);
	
		
	}
	
	private String nameListToString( List<String> names) {
		StringBuffer buffer = new StringBuffer();
		for (String name : names) {
			if (buffer.length() > 0)
				buffer.append( ",");
			buffer.append( name);
		}
		return buffer.toString();
	}
	
	/**
	 * tests no rule -> any  
	 */
	@Test
	public void noRuleTest() {
		test( null, Arrays.asList( 
				"com.braintribe.devrock.test.tags:none#1.0",
				"com.braintribe.devrock.test.tags:standard#1.0",
				"com.braintribe.devrock.test.tags:war#1.0",
				"com.braintribe.devrock.test.tags:asset-man#1.0",
				"com.braintribe.devrock.test.tags:asset-other#1.0",
				"com.braintribe.devrock.test.tags:zip#1.0"
		));
	}
	
	@Test
	public void JarRuleTest() {
		test( "JAR", Arrays.asList( 		
				"com.braintribe.devrock.test.tags:none#1.0",
				"com.braintribe.devrock.test.tags:standard#1.0"				
		));
	}
	
	@Test
	public void ClassifierTest() {
		test( "asset:", Arrays.asList( 		
				"com.braintribe.devrock.test.tags:asset-man#1.0",
				"com.braintribe.devrock.test.tags:asset-other#1.0"
		));
	}
	
	@Test
	public void ManTest() {
		test( "man", Arrays.asList( 		
				"com.braintribe.devrock.test.tags:asset-man#1.0",
				"com.braintribe.devrock.test.tags:noasset-man#1.0"
		));
	}

	@Test
	public void AssetManTest() {
		test( "asset:man", Arrays.asList( 		
				"com.braintribe.devrock.test.tags:asset-man#1.0"				
		));
	}
	
	@Test
	public void WarRuleTest() {
		test( "war", Arrays.asList( 		
				"com.braintribe.devrock.test.tags:war#1.0"				
		));
	}
	
	@Test
	public void ZipRuleTest() {
		test( "zip", Arrays.asList( 		
				"com.braintribe.devrock.test.tags:zip#1.0"
		));
	}
	@Test
	public void namedCombinedRuleTest() {
		test( "asset:,man,asset:man", Arrays.asList( 		
				"com.braintribe.devrock.test.tags:asset-man#1.0",
				"com.braintribe.devrock.test.tags:asset-other#1.0",
				"com.braintribe.devrock.test.tags:noasset-man#1.0"
		));
	}

}
