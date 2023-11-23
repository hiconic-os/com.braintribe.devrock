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
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.test.multi.wire.AbstractRepoletWalkerWireTest;
import com.braintribe.wire.api.context.WireContext;

public class OptionalInDependenciesIssueLab extends AbstractRepoletWalkerWireTest {
	
private static final String VRS = "1.0";
private static final String ART = "terminal";
private static final String GRP = "com.braintribe.devrock.test.optionals";
private Map<String, RepoType> launcherMap;
	
	{
		launcherMap = new HashMap<>();		
		launcherMap.put( "archive," + new File( testSetup, "archive.zip").getAbsolutePath(), RepoType.singleZip);
						
	}
	
	@Override
	protected File getRoot() {	
		return new File("res/wire/issues/optionals");
	}
		
	@Before
	public void before() {
		runBefore(launcherMap);
	}
	
	@After
	public void after() {
		runAfter();
	}
	
	

	
	private void test(boolean skipOptionals, String [] expected) {
		try {
			WireContext<ClasspathResolverContract> classpathWalkContext = getClasspathWalkContext( new File( testSetup, "settings.xml"), repo, overridesMap);			
			
			WalkerContext walkerContext = new WalkerContext();
			walkerContext.setSkipOptionals( skipOptionals);
			
			Walker walker = classpathWalkContext.contract().walker( walkerContext);
			
			String walkScopeId = UUID.randomUUID().toString();
			
			Solution terminal = Solution.T.create();
			terminal.setGroupId( GRP);
			terminal.setArtifactId( ART);
			terminal.setVersion( VersionProcessor.createFromString(VRS));
			
			Collection<Solution> collection = walker.walk( walkScopeId, terminal);
			System.out.println("found [" + collection.size() + "] dependencies");
			collection.stream().forEach( s -> System.out.println(NameParser.buildName(s)));
			validate( collection, expected);
		} catch (Exception e) {

			e.printStackTrace();
		} 
	}
	
	@Test
	public void skipOptionals() {
		String [] expected = new String [] { GRP + ":" + "a" + "#" + VRS,
											GRP + ":" + "a-optional" + "#" + VRS,
											GRP + ":" + "b" + "#" + VRS,
											};
		test( true, expected);
	}
	
	@Test
	public void includeOptionals() {
		String [] expected = new String [] { GRP + ":" + "a" + "#" + VRS,
				GRP + ":" + "a-optional" + "#" + VRS,
				GRP + ":" + "b" + "#" + VRS,
				GRP + ":" + "b-optional" + "#" + VRS,
				};
		test( false, expected);
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
