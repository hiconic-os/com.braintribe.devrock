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
package com.braintribe.test.multi.biasLab;

import java.io.File;
import java.util.Collection;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.denotations.WalkKind;
import com.braintribe.test.framework.TestUtil;

public class ComplexBiasTestLab extends AbstractComplexBiasLab {
	
	
protected static final File contents = new File("res/biasLab2/contents");
protected static File settings = new File( contents,"/settings.xml");
protected static File bias = new File( contents, "pc_bias.local.txt");

@BeforeClass
public static void beforeClass() {
	before( settings);
}


@Override
protected void testPresence(Collection<Solution> solutions, File repository) {
	super.testPresence(solutions, repository);
}	

@Before
public void before() {
	// clean local repository
	TestUtil.delete(localRepository);
	
	// copy everything from base
	TestUtil.copy(base, localRepository);		
	
	repositoryRegistry.clear();
}


@Test
public void testImplicitLocalBias() {
	TestUtil.copy(bias, localRepository, ".pc_bias");
	String[] expectedNames = new String [] {					
			"com.braintribe.devrock.test.bias:a#1.0.2-pc", // bias on this artifact -> not the highest version, but the highest pc 
			"com.braintribe.devrock.test.bias:b#1.0.1", // 					
	};
	
	runTest( "com.braintribe.devrock.test.bias:bias-terminal#1.0.1", expectedNames, ScopeKind.compile, WalkKind.classpath, false);
}

@Test
public void testExplicitLocalBias() {
	TestUtil.copy( new File(contents, "pc_bias.local.standard.txt"), localRepository, ".pc_bias");
	String[] expectedNames = new String [] {					
			"com.braintribe.devrock.test.bias:a#1.0.2-pc", // bias on this artifact -> not the highest version, but the highest pc 
			"com.braintribe.devrock.test.bias:b#1.0.1", // 					
	};
	
	runTest( "com.braintribe.devrock.test.bias:bias-terminal#1.0.1", expectedNames, ScopeKind.compile, WalkKind.classpath, false);
}

@Test
public void testNoBias() {
	TestUtil.delete( new File( localRepository, ".pc_bias"));
	String[] expectedNames = new String [] {					
			"com.braintribe.devrock.test.bias:a#1.0.3", // bias on this artifact -> not the highest version, but the highest pc 
			"com.braintribe.devrock.test.bias:b#1.0.1", // 					
	};
	
	runTest( "com.braintribe.devrock.test.bias:bias-terminal#1.0.1", expectedNames, ScopeKind.compile, WalkKind.classpath, false);
}

@Test
public void testBiasNegativeBias() {
	TestUtil.copy( new File( contents, "pc_bias.!D.txt"), localRepository, ".pc_bias");
	String[] expectedNames = new String [] {					
			"com.braintribe.devrock.test.bias:a#1.0.2", // bias on this artifact -> not the highest version, but the highest pc 
			"com.braintribe.devrock.test.bias:b#1.0.1", // 					
	};
	
	runTest( "com.braintribe.devrock.test.bias:bias-terminal#1.0.1", expectedNames, ScopeKind.compile, WalkKind.classpath, false);
}

@Test
public void testBiasPositiveBias() {
	TestUtil.copy( new File( contents, "pc_bias.positive.BC.txt"), localRepository, ".pc_bias");
	String[] expectedNames = new String [] {					
			"com.braintribe.devrock.test.bias:a#1.0.2", // bias on this artifact -> not the highest version, but the highest pc 
			"com.braintribe.devrock.test.bias:b#1.0.1", // 					
	};
	
	runTest( "com.braintribe.devrock.test.bias:bias-terminal#1.0.1", expectedNames, ScopeKind.compile, WalkKind.classpath, false);
}

@Test
public void testBiasNoLocalBias() {
	TestUtil.copy( new File( contents, "pc_bias.no_local.txt"), localRepository, ".pc_bias");
	String[] expectedNames = new String [] {					
			"com.braintribe.devrock.test.bias:a#1.0.1", // bias on this artifact -> not the highest version, but the highest pc 
			"com.braintribe.devrock.test.bias:b#1.0.1", // 					
	};
	
	runTest( "com.braintribe.devrock.test.bias:bias-terminal#1.0.1", expectedNames, ScopeKind.compile, WalkKind.classpath, false);
}



}
