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
