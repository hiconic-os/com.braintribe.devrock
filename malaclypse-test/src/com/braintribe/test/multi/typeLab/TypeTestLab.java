// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
package com.braintribe.test.multi.typeLab;

import java.io.File;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.denotations.WalkKind;


/**
 *
 * 
 * 
 *
 * @author pit
 *
 */
public class TypeTestLab extends AbstractTypeLab {
		
	
	protected static File settings = new File( "res/typeTest/contents/settings.xml");
	protected static String group = "com.braintribe.devrock.test.types";
	protected static String version = "1.0.1";
	
	@BeforeClass
	public static void before() {
		before( settings);
	}


	@Override
	protected void testPresence(Collection<Solution> solutions, File repository) {
		super.testPresence(solutions, repository);
	}	
	
	@Test
	public void testNullRule() {
		String[] expectedNames = new String [] {						
				group + ":" + "none" + "#" + version, // no packaging -> jar
				group + ":" + "standard" + "#" + version, // jar packaging
				group + ":" + "war" + "#" + version, // war packaging
				group + ":" + "zip" + "#" + version, // zip packaging
				group + ":" + "asset-man" + "#" + version, // man packaging
				group + ":" + "noasset-man" + "#" + version, // man packaging
				group + ":" + "asset-other" + "#" + version, // other packaging										
		};
		
		runTest( null, group + ":" + "types-terminal" + "#" + version, expectedNames, ScopeKind.compile, WalkKind.classpath, false);
	}
	
	@Test
	public void testJarRule() {
		String[] expectedNames = new String [] {						
				group + ":" + "none" + "#" + version, // no packaging -> jar
				group + ":" + "standard" + "#" + version, // jar packaging
												
		};
		
		runTest( "jar", group + ":" + "types-terminal" + "#" + version, expectedNames, ScopeKind.compile, WalkKind.classpath, false);
	}
	
	@Test
	public void testClassifierRule() {
		String[] expectedNames = new String [] {						
				
				group + ":" + "asset-man" + "#" + version, // man packaging
				
				group + ":" + "asset-other" + "#" + version, // other packaging										
		};
		
		runTest( "asset:", group + ":" + "types-terminal" + "#" + version, expectedNames, ScopeKind.compile, WalkKind.classpath, false);
	}
	
	@Test
	public void testManRule() {
		String[] expectedNames = new String [] {						
				
				group + ":" + "asset-man" + "#" + version, // man packaging
				group + ":" + "noasset-man" + "#" + version, // man packaging
														
		};
		
		runTest( "man", group + ":" + "types-terminal" + "#" + version, expectedNames, ScopeKind.compile, WalkKind.classpath, false);
	}
	
	
	@Test
	public void testClassifierAndTypeRule() {
		String[] expectedNames = new String [] {						
				group + ":" + "asset-man" + "#" + version, // man packaging														
		};
		
		runTest( "asset:man", group + ":" + "types-terminal" + "#" + version, expectedNames, ScopeKind.compile, WalkKind.classpath, false);
	}
	
	@Test
	public void testCombinedRule() {
		String[] expectedNames = new String [] {						
				
				group + ":" + "asset-man" + "#" + version, // man packaging
				group + ":" + "noasset-man" + "#" + version, // man packaging
				group + ":" + "asset-other" + "#" + version, // other packaging										
		};
		
		runTest( "asset:,asset:man,man", group + ":" + "types-terminal" + "#" + version, expectedNames, ScopeKind.compile, WalkKind.classpath, false);
	}
	
	@Test
	public void testDenotingRule() {
		String[] expectedNames = new String [] {						
				group + ":" + "war" + "#" + version, // war packaging
				group + ":" + "zip" + "#" + version, // zip packaging
				group + ":" + "asset-man" + "#" + version, // man packaging
				group + ":" + "noasset-man" + "#" + version, // man packaging
														
		};
		
		runTest( "war,zip,man", group + ":" + "types-terminal" + "#" + version, expectedNames, ScopeKind.compile, WalkKind.classpath, false);
	}
	
}
