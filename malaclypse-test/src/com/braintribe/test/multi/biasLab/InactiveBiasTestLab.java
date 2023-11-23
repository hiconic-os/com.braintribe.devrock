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

import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.denotations.WalkKind;


/**
 *
 * this tests MC's support for "publishing candidate bias" - <b>deactivation</b><br/>
 * it reacts on the .pc_bias file in the local repository, which tells MC to only reflect 
 * locally installed versions, no matter what's in the remote repo
 * <br/> 
 *
 * <br/>
 * com.braintribe.devrock.test.bias:bias-terminal#1.0.1
 * ->
 * com.braintribe.devrock.test.bias:a#1.0.3
 * com.braintribe.devrock.test.bias:b#1.0.1
 * 
 * existing in remote repo are 
 * com.braintribe.devrock.test.bias:a#1.0.1
 * com.braintribe.devrock.test.bias:a#1.0.3
 * @author pit
 *
 */
public class InactiveBiasTestLab extends AbstractBiasLab {
		
	
	protected static File settings = new File( "res/typeTest/contents/settings.xml");
	
	@BeforeClass
	public static void before() {
		before( settings);
	}


	@Override
	protected void testPresence(Collection<Solution> solutions, File repository) {
		super.testPresence(solutions, repository);
	}	
	
	@Test
	public void testInactiveBias() {
		String[] expectedNames = new String [] {					
				"com.braintribe.devrock.test.bias:a#1.0.3", // no bias on this artifact -> this is the highest version
				"com.braintribe.devrock.test.bias:b#1.0.1", // 					
		};
		
		runTest( "com.braintribe.devrock.test.bias:bias-terminal#1.0.1", expectedNames, ScopeKind.compile, WalkKind.classpath, false);
	}
	
	
	

	
}
