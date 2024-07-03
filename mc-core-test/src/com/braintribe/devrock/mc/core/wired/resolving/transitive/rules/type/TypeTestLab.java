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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.rules.type;

import java.io.File;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.KnownIssue;


/**
 * @author pit
 */

//TODO : once filters are properly inserted into TDR/CPR the test can succeed
@Category( KnownIssue.class)
public class TypeTestLab extends AbstractTypeLab {
		
	
	protected static File settings = new File( "res/typeTest/contents/settings.xml");
	protected static String group = "com.braintribe.devrock.test.types";
	protected static String version = "1.0.1";
	
	
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
		
		runTest( null, group + ":" + "types-terminal" + "#" + version, expectedNames);
	}
	
	@Test
	public void testJarRule() {
		String[] expectedNames = new String [] {						
				group + ":" + "none" + "#" + version, // no packaging -> jar
				group + ":" + "standard" + "#" + version, // jar packaging
												
		};
		
		runTest( "jar", group + ":" + "types-terminal" + "#" + version, expectedNames);
	}
	
	@Test
	public void testClassifierRule() {
		String[] expectedNames = new String [] {						
				
				group + ":" + "asset-man" + "#" + version, // man packaging
				
				group + ":" + "asset-other" + "#" + version, // other packaging										
		};
		
		runTest( "asset:", group + ":" + "types-terminal" + "#" + version, expectedNames);
	}
	
	@Test
	public void testManRule() {
		String[] expectedNames = new String [] {						
				
				group + ":" + "asset-man" + "#" + version, // man packaging
				group + ":" + "noasset-man" + "#" + version, // man packaging
														
		};
		
		runTest( "man", group + ":" + "types-terminal" + "#" + version, expectedNames);
	}
	
	
	@Test
	public void testClassifierAndTypeRule() {
		String[] expectedNames = new String [] {						
				group + ":" + "asset-man" + "#" + version, // man packaging														
		};
		
		runTest( "asset:man", group + ":" + "types-terminal" + "#" + version, expectedNames);
	}
	
	@Test
	public void testCombinedRule() {
		String[] expectedNames = new String [] {						
				
				group + ":" + "asset-man" + "#" + version, // man packaging
				group + ":" + "noasset-man" + "#" + version, // man packaging
				group + ":" + "asset-other" + "#" + version, // other packaging										
		};
		
		runTest( "asset:,asset:man,man", group + ":" + "types-terminal" + "#" + version, expectedNames);
	}
	
	@Test
	public void testDenotingRule() {
		String[] expectedNames = new String [] {						
				group + ":" + "war" + "#" + version, // war packaging
				group + ":" + "zip" + "#" + version, // zip packaging
				group + ":" + "asset-man" + "#" + version, // man packaging
				group + ":" + "noasset-man" + "#" + version, // man packaging
														
		};
		
		runTest( "war,zip,man", group + ":" + "types-terminal" + "#" + version, expectedNames);
	}
	
}
