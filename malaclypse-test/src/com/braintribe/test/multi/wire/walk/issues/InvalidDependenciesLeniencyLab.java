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
package com.braintribe.test.multi.wire.walk.issues;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.test.repolet.LauncherShell.RepoType;
import com.braintribe.build.artifact.walk.multi.Walker;
import com.braintribe.build.artifacts.mc.wire.classwalk.context.WalkerContext;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.model.artifact.Solution;
import com.braintribe.test.multi.wire.AbstractRepoletWalkerWireTest;
import com.braintribe.wire.api.context.WireContext;



public class InvalidDependenciesLeniencyLab extends AbstractRepoletWalkerWireTest {
	
private static final String VRS = "1.0.1-pc";
private static final String ART = "invalids-terminal";
private static final String GRP = "com.braintribe.devrock.test.invalids";
private Map<String, RepoType> launcherMap;
	
	{
		launcherMap = new HashMap<>();		
		launcherMap.put( "archive," + new File( testSetup, "archive.zip").getAbsolutePath(), RepoType.singleZip);
						
	}
	
	@Override
	protected File getRoot() {	
		return new File("res/wire/issues/resolutionLeniency");
	}
		
	@Before
	public void before() {
		runBefore(launcherMap);
	}
	
	@After
	public void after() {
		runAfter();
	}
	
	

	
	private void test(String condensedTerminal, boolean expansionLeniency, String [] expected) {
		boolean hitException = false;
		try {
			WireContext<ClasspathResolverContract> classpathWalkContext = getClasspathWalkContext( new File( testSetup, "settings.xml"), repo, overridesMap);
						
			WalkerContext walkerContext = new WalkerContext();
			walkerContext.setSkipOptionals(true);
			
			Walker walker = classpathWalkContext.contract().walker( walkerContext);
			
			String walkScopeId = UUID.randomUUID().toString();
			
			Solution terminal = NameParser.parseCondensedSolutionName(condensedTerminal);
			
			classpathWalkContext.contract().pomReader().setExpansionLeniency( expansionLeniency);
			
			Collection<Solution> collection = walker.walk( walkScopeId, terminal);
			System.out.println("found [" + collection.size() + "] dependencies");
		
			collection.stream().forEach( s -> System.out.println(NameParser.buildName(s)));
			
			validate( collection, expected);
		
		} catch (Exception e) {
			//e.printStackTrace();
			hitException = true;
		} 
		
		if (!expansionLeniency) {
			if (!hitException) {
				fail("expected exception not thrown");
			}
		}
	}
	
	
	@Test
	public void lenientRun() {
		String [] expected = new String [] { 
				GRP + ":" + "a" + "#" + VRS,
				GRP + ":" + "b" + "#" + VRS,			
				};
		test( GRP + ":" + ART + "#" + VRS, true, expected);
	}
	
	@Test
	public void lenientFailRun() {
		String [] expected = new String [] { 
				GRP + ":" + "a" + "#" + VRS,
				GRP + ":" + "b" + "#" + VRS,			
				};
		test( GRP + ":" + "a" + "#" + VRS, true, expected);
	}
	
	@Test
	public void strictRun() {
		String [] expected = new String [] { 
				GRP + ":" + "a" + "#" + VRS,
				GRP + ":" + "b" + "#" + VRS,			
				};
		test( GRP + ":" + ART + "#" + VRS, false, expected);
	}
	
	
	
	private String collate( List<String> str) {
		return str.stream().collect(Collectors.joining( ","));
	}
	
	private void validate( Collection<Solution> result, String [] expected) {
		List<String> exp = new ArrayList<>( Arrays.asList( expected));
		List<String> fnd = result.stream().map( s -> NameParser.buildName(s)).collect(Collectors.toList());
		List<String> matched = new ArrayList<>();
		List<String> missing = new ArrayList<>();
		for (String found : fnd) {
			if (exp.contains(found)) {
				matched.add(found);
			}
			else {
				missing.add( found);
			}
		}
		exp.removeAll(matched);
		assertTrue( "expected, but missing [" + collate( missing) + "]", missing.size() == 0);
		assertTrue( "found, but unexpected [" + collate( exp) + "]", exp.size() == 0);
	}

}
