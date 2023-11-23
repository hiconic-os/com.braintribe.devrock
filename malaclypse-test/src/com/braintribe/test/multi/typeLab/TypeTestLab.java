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
