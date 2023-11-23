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
package com.braintribe.test.merge;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.name.NameParserException;
import com.braintribe.build.artifact.walk.multi.clash.merger.DependencyMerger;
import com.braintribe.build.artifact.walk.multi.clash.merger.DependencyMergerImpl;
import com.braintribe.model.artifact.Dependency;

public class DependencyRangeMerging {

	
	private static final String SCOPE_ID = "na";
	private String [] noMergeNames = new String [] { "a.b.c:A#1.0", "a.b.c:A#2.0"};
	private String [] innerIntervalNames = new String [] {"a.b.c:A#[1.0, 2.0]", "a.b.c:A#[1.1, 1.9]"};
	private String [] intersectingIntervalNames = new String [] {"a.b.c:A#[1.0, 1.6]", "a.b.c:A#[1.5, 2.0]"};
	private String [] intersectingIntervalShortNames = new String [] {"a.b.c:A#1.6.^", "a.b.c:A#[1.6.1, 1.6.8]"};	
	private String [] intersectingIntervalShortNames2 = new String [] {"a.b.c:A#[1.6.1, 1.6.8]", "a.b.c:A#1.6.^", };
	private String [] intersectingIntervalShortNames3 = new String [] {"a.b.c:A#1.6.2.^", "a.b.c:A#[1.6.1, 1.6.8]"};
	private String [] noMergeIntervalNames = new String [] {"a.b.c:A#[1.0, 1.9]", "a.b.c:A#[2.0, 2.1]"};
	private String [] collapsingIntervalNames = new String [] {"a.b.c:A#[1.0, 2.0]", "a.b.c:A#[2.0, 2.1]"};
	private String [] collapsingClosedIntervalNames = new String [] {"a.b.c:A#[1.0, 2.0)", "a.b.c:A#(2.0, 2.1]"};
	private String [] mergeToSingleVersionNames = new String[] {"a.b.c:A#[1.0, 2.0)", "a.b.c:A#[1.4,1.6]"};
		
	
	private DependencyMerger dependencyMerger = new DependencyMergerImpl();

	
	private Collection<Dependency> createDependenciesFromStrings( String [] names) {
		Collection<Dependency> dependencies = new ArrayList<Dependency>();
		for (String name : names) {
			try {
				Dependency dependency = NameParser.parseCondensedDependencyName(name);
				dependencies.add( dependency);
			} catch (NameParserException e) {
				fail( "exception thrown while parsing [" + name + "]:" + e);
			}
		}
		return dependencies;
	}
	
	private void listDependencies( Collection<Dependency> dependencies) {
		for (Dependency dependency : dependencies) {
			System.out.println(NameParser.buildName( dependency));
		}
	}

	@Test
	public void noMergetest() {
		Collection<Dependency> suspects = createDependenciesFromStrings(noMergeNames);
		Collection<Dependency> result = dependencyMerger.mergeDependencies(SCOPE_ID, suspects, new ArrayList<Dependency>());
		
		Assert.assertTrue( "merge has taken place", suspects.size() == result.size());
	}
	@Test
	public void noMergeIntervalTest() {
		System.out.println("**** no merge intersecting interval ****");
		Collection<Dependency> suspects = createDependenciesFromStrings(noMergeIntervalNames);
		Collection<Dependency> result =  dependencyMerger.mergeDependencies(SCOPE_ID, suspects, new ArrayList<Dependency>());
		System.out.println("result");		
		listDependencies(result);
		Assert.assertTrue( "merge has taken place", suspects.size() == result.size());
		
	}
	
	@Test
	public void mergeInnerIntervaltest() {
		System.out.println("**** inner interval ****");
		Collection<Dependency> suspects = createDependenciesFromStrings( innerIntervalNames);
		listDependencies( suspects);
		
		Collection<Dependency> result =  dependencyMerger.mergeDependencies(SCOPE_ID, suspects, new ArrayList<Dependency>());
		System.out.println("result");
		listDependencies(result);
		
		Assert.assertTrue( "merge has not taken place", suspects.size() != result.size());
		
	}
	
	@Test
	public void mergeIntersectingIntervaltest() {
		System.out.println("**** intersecting interval ****");
		Collection<Dependency> suspects = createDependenciesFromStrings( intersectingIntervalNames);
		listDependencies( suspects);
		
		Collection<Dependency> result =  dependencyMerger.mergeDependencies(SCOPE_ID, suspects, new ArrayList<Dependency>());
		
		Assert.assertTrue( "merge has not taken place", suspects.size() != result.size());
		//Dependency resultingDependency = result.toArray( new Dependency[0])[0];
		System.out.println("result");
		listDependencies(result);
		
	}
	@Test
	public void mergeIntersectingIntervalShortNameTest() {
		System.out.println("**** intersecting fuzzy interval ****");
		Collection<Dependency> suspects = createDependenciesFromStrings( intersectingIntervalShortNames);
		listDependencies( suspects);
		
		Collection<Dependency> result =  dependencyMerger.mergeDependencies(SCOPE_ID, suspects, new ArrayList<Dependency>());
		
		System.out.println("result");
		listDependencies(result);
		
		Assert.assertTrue( "merge has not taken place", suspects.size() != result.size());
	}
	
	@Test
	public void mergeIntersectingIntervalShortNameTest2() {
		System.out.println("**** intersecting fuzzy interval reversed****");
		Collection<Dependency> suspects = createDependenciesFromStrings( intersectingIntervalShortNames2);
		listDependencies( suspects);
		
		Collection<Dependency> result = dependencyMerger.mergeDependencies(SCOPE_ID, suspects, new ArrayList<Dependency>());
		
		//Dependency resultingDependency = result.toArray( new Dependency[0])[0];
		System.out.println("result");
		listDependencies(result);		
		Assert.assertTrue( "merge has not taken place", suspects.size() != result.size());
	}
	
	@Test
	public void mergeIntersectingIntervalShortNameTest3() {
		System.out.println("**** intersecting fuzzy interval indexed ****");
		Collection<Dependency> suspects = createDependenciesFromStrings( intersectingIntervalShortNames3);
		listDependencies( suspects);
		
		Collection<Dependency> result =  dependencyMerger.mergeDependencies(SCOPE_ID, suspects, new ArrayList<Dependency>());
		
		//Dependency resultingDependency = result.toArray( new Dependency[0])[0];
		System.out.println("result");
		listDependencies(result);
		Assert.assertTrue( "merge has not taken place", suspects.size() != result.size());
		
	}
	
	/**
	 * [1.0, 2.0] & [2.0, 2.1] should actually lead to [2.0,2.0], but the question is how represent that.. 
	 */
	@Test
	public void mergeCollapsingIntervalNameTest() {
		System.out.println("**** collapsing interval ****");
		Collection<Dependency> suspects = createDependenciesFromStrings( collapsingIntervalNames);
		listDependencies( suspects);
		
		Collection<Dependency> result =  dependencyMerger.mergeDependencies(SCOPE_ID, suspects, new ArrayList<Dependency>());
		
		System.out.println("result");
		listDependencies(result);		
		Assert.assertTrue( "merge has taken place", suspects.size() == result.size());		
	}
	@Test
	public void mergeCollapsingClosedIntervalNameTest() {
		System.out.println("**** collapsing closed interval ****");
		Collection<Dependency> suspects = createDependenciesFromStrings( collapsingClosedIntervalNames);
		listDependencies( suspects);
		
		Collection<Dependency> result =  dependencyMerger.mergeDependencies(SCOPE_ID, suspects, new ArrayList<Dependency>());
		
		System.out.println("result");
		listDependencies(result);		
		Assert.assertTrue( "merge has taken place", suspects.size() == result.size());		
	}
	
	@Test
	public void mergeCollapsingToSingleNameTest() {
		System.out.println("**** collapsing to single version ****");
		Collection<Dependency> suspects = createDependenciesFromStrings( mergeToSingleVersionNames);
		listDependencies( suspects);
		
		Collection<Dependency> result =  dependencyMerger.mergeDependencies(SCOPE_ID, suspects, new ArrayList<Dependency>());
		
		System.out.println("result");
		listDependencies(result);		
		Assert.assertTrue( "merge has not taken place", suspects.size() != result.size());		
	}
	
	

}
