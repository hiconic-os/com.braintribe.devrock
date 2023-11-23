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
package com.braintribe.devrock.mc.core.ruleFilter.tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.core.resolver.rulefilter.BasicTagRuleFilter;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.version.VersionExpression;
import com.braintribe.wire.api.util.Lists;


/**
 * @author pit
 */
public class TagRuleMatcherLab  {
	
	private static List<AnalysisDependency> dependencies;
	private static final String groupId = "com.braintribe.devrock.test.tags";
	private static final String ve = "1.0"; 	
	
	{
		dependencies = new ArrayList<>();
		
		dependencies.add( create( ArtifactIdentification.create(groupId, "none"), ve, Collections.emptyList()));
		
		dependencies.add( create(ArtifactIdentification.create(groupId, "standard"), ve, Collections.singletonList( "standard")));
				
		dependencies.add( create(ArtifactIdentification.create(groupId, "one"), ve, Collections.singletonList( "one")));
		
		dependencies.add( create(ArtifactIdentification.create(groupId, "one-and-two"), ve, Lists.list( "one" , "two")));
		
		dependencies.add( create(ArtifactIdentification.create(groupId, "classpath"), ve, Collections.singletonList("asset")));
				
	}
	
	/**
	 * @param ai
	 * @param version
	 * @param tags
	 * @return
	 */
	private AnalysisDependency create( ArtifactIdentification ai, String version, List<String> tags) {
		AnalysisDependency ad = AnalysisDependency.T.create();
		ad.setGroupId( ai.getGroupId());
		ad.setArtifactId( ai.getArtifactId());
		ad.setVersion( version);
		
		VersionExpression ve = VersionExpression.parse(version);
		
		CompiledDependency cd = CompiledDependency.create(ai.getGroupId(), ai.getArtifactId(), ve, "compile");
		cd.getTags().addAll( tags);		
		ad.setOrigin(cd);
		
		return ad;
	}
	
	/**
	 * @param rule
	 * @param expectedNames
	 */
	private void test( String rule, List<String> expectedNames) {
		BasicTagRuleFilter matcher = new BasicTagRuleFilter();
		matcher.setRule(rule);
		
		List<String> matching = new ArrayList<>();
		List<String> excess = new ArrayList<>();
		
		List<AnalysisDependency> filtered = matcher.filter(dependencies);
		
		for (AnalysisDependency dependency : filtered) {
			String name = dependency.asString();			
			if (expectedNames.contains(name)) {
				matching.add( name);
			}
			else {
				excess.add( name);
			}
		}
		
		List<String> missing = new ArrayList<>( expectedNames);
		missing.removeAll( matching);
	
		Assert.assertTrue( "missing tags are [" + collate( missing) + "]", missing.size() == 0);
		Assert.assertTrue( "excess tags are [" + collate( excess) + "]", excess.size() == 0);
		
	}
	
	private String collate( List<String> names) {
		return names.stream().collect(Collectors.joining(","));
	}	
	
	@Test
	public void noRuleTest() {
		test( null, Arrays.asList( 
				"com.braintribe.devrock.test.tags:none#1.0",
				"com.braintribe.devrock.test.tags:standard#1.0",
				"com.braintribe.devrock.test.tags:one#1.0",
				"com.braintribe.devrock.test.tags:one-and-two#1.0",
				"com.braintribe.devrock.test.tags:classpath#1.0"
		));
	}
	
	@Test
	public void allInRuleTest() {
		test( "*", Arrays.asList( 		
				"com.braintribe.devrock.test.tags:standard#1.0",
				"com.braintribe.devrock.test.tags:one#1.0",
				"com.braintribe.devrock.test.tags:one-and-two#1.0",
				"com.braintribe.devrock.test.tags:classpath#1.0"
		));
	}
	
	@Test
	public void noneInRuleTest() {
		test( "!*", Arrays.asList( 		
				"com.braintribe.devrock.test.tags:none#1.0"			
		));
	}
	
	@Test
	public void namedExclusionRuleTest() {
		test( "!one", Arrays.asList( 		
				"com.braintribe.devrock.test.tags:none#1.0",
				"com.braintribe.devrock.test.tags:standard#1.0",
				"com.braintribe.devrock.test.tags:classpath#1.0"
		));
	}
	
	@Test
	public void namedInclusionRuleTest() {
		test( "one", Arrays.asList( 		
				"com.braintribe.devrock.test.tags:one#1.0",
				"com.braintribe.devrock.test.tags:one-and-two#1.0"
		));
	}
	@Test
	public void namedCombinedRuleTest() {
		test( "one,!two", Arrays.asList( 		
				"com.braintribe.devrock.test.tags:one#1.0"				
		));
	}

}
