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
package com.braintribe.devrock.mc.core.ruleFilter.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.core.resolver.rulefilter.BasicTypeRuleFilter;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.version.VersionExpression;


/**
 * tests the type-rule filters directly 
 * 
 * @author pit
 *
 */
public class TypeRuleMatcherLab {
	
	private static List<AnalysisDependency> dependencies;
	private static final String groupId = "com.braintribe.devrock.test.types";
	private static final String version = "1.0";
	
	{
		dependencies = new ArrayList<>();
		
		dependencies.add( create( ArtifactIdentification.create(groupId, "none"), version, PartIdentification.T.create()));
		
		dependencies.add( create( ArtifactIdentification.create(groupId, "standard"), version, PartIdentification.parse("jar")));
		
		dependencies.add( create( ArtifactIdentification.create(groupId, "war"), version, PartIdentification.parse("war")));		
		
		dependencies.add( create( ArtifactIdentification.create(groupId, "asset-man"), version, PartIdentification.parse("asset:man")));		
		
		dependencies.add( create( ArtifactIdentification.create(groupId, "asset-other"), version, PartIdentification.parse("asset:other")));
		
		dependencies.add( create( ArtifactIdentification.create(groupId, "noasset-man"), version, PartIdentification.parse(":man")));
				
		dependencies.add( create( ArtifactIdentification.create(groupId, "zip"), version, PartIdentification.parse("zip")));		
	}
	
	/**
	 * create a {@link AnalysisDependency} plus its origin the {@link CompiledDependency}
	 * @param ai - the {@link ArtifactIdentification}
	 * @param version - the version
	 * @param pi - the {@link PartIdentification}
	 * @return - the created {@link AnalysisDependency}
	 */
	private AnalysisDependency create( ArtifactIdentification ai, String version, PartIdentification pi) {
		AnalysisDependency ad = AnalysisDependency.T.create();
		ad.setGroupId( ai.getGroupId());
		ad.setArtifactId( ai.getArtifactId());
		ad.setVersion( version);
		ad.setClassifier( pi.getClassifier());
		ad.setType( pi.getType());
		
		VersionExpression ve = VersionExpression.parse(version);
		
		CompiledDependency cd = CompiledDependency.create(ai.getGroupId(), ai.getArtifactId(), ve, "compile", pi.getClassifier(), pi.getType());		
		ad.setOrigin(cd);
		
		return ad;
	}
	
	/**
	 * run the test 
	 * @param rule - the type-rule 
	 * @param expectedNames - the names of the expected dependencies that pass the filter 
	 */
	private void test( String rule, List<String> expectedNames) {
		BasicTypeRuleFilter matcher = new BasicTypeRuleFilter();
		matcher.setRule(rule);
		
		List<String> excess = new ArrayList<>();
		List<String> matching = new ArrayList<>();
		
		List<AnalysisDependency> filtered = matcher.filter(dependencies);
		List<String> founds = filtered.stream().map( ad -> {
				return ad.getGroupId() + ":" + ad.getArtifactId() + "#" + ad.getVersion();
			}).collect( Collectors.toList());
		
				
		for (String found : founds) {								
			if (expectedNames.contains( found)) {
				matching.add( found);				
			}
			else {
				excess.add( found);
			}
		}
		
		Assert.assertTrue( rule + " : excess [" + collate(excess) + "]", excess.size() == 0);
		
		List<String> missing = new ArrayList<>( expectedNames);
		missing.removeAll( matching);
		
		
		Assert.assertTrue( rule + " : missing [" + collate( missing) + "]", missing.size() == 0);
	
		
	}
	
	/**
	 * @param names
	 * @return
	 */
	private String collate( List<String> names) {
		return names.stream().collect(Collectors.joining(","));
	}
	
	/**
	 * tests no rule -> any  
	 */
	@Test
	public void noRuleTest() {
		test( null, Arrays.asList( 
				"com.braintribe.devrock.test.types:none#1.0",
				"com.braintribe.devrock.test.types:standard#1.0",
				"com.braintribe.devrock.test.types:war#1.0",
				"com.braintribe.devrock.test.types:asset-man#1.0",
				"com.braintribe.devrock.test.types:asset-other#1.0",
				"com.braintribe.devrock.test.types:noasset-man#1.0",
				"com.braintribe.devrock.test.types:zip#1.0"
		));
	}
	
	@Test
	public void JarRuleTest() {
		test( "JAR", Arrays.asList( 		
				"com.braintribe.devrock.test.types:none#1.0",
				"com.braintribe.devrock.test.types:standard#1.0"				
		));
	}
	
	@Test
	public void ClassifierTest() {
		test( "asset:", Arrays.asList( 		
				"com.braintribe.devrock.test.types:asset-man#1.0",
				"com.braintribe.devrock.test.types:asset-other#1.0"
		));
	}
	
	@Test
	public void ManTest() {
		test( "man", Arrays.asList( 		
				"com.braintribe.devrock.test.types:asset-man#1.0",
				"com.braintribe.devrock.test.types:noasset-man#1.0"
		));
	}

	@Test
	public void AssetManTest() {
		test( "asset:man", Arrays.asList( 		
				"com.braintribe.devrock.test.types:asset-man#1.0"				
		));
	}
	
	@Test
	public void WarRuleTest() {
		test( "war", Arrays.asList( 		
				"com.braintribe.devrock.test.types:war#1.0"				
		));
	}
	
	@Test
	public void ZipRuleTest() {
		test( "zip", Arrays.asList( 		
				"com.braintribe.devrock.test.types:zip#1.0"
		));
	}
	@Test
	public void namedCombinedRuleTest() {
		test( "asset:,man,asset:man", Arrays.asList( 		
				"com.braintribe.devrock.test.types:asset-man#1.0",
				"com.braintribe.devrock.test.types:asset-other#1.0",
				"com.braintribe.devrock.test.types:noasset-man#1.0"
		));
	}

}
