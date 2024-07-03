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

import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.denotations.WalkKind;
import com.braintribe.test.framework.TestUtil;


/**
 *
 * this tests MC's support for "publishing candidate bias"
 * it reacts on the .pc_bias file in the local repository, which tells MC to only reflect 
 * locally installed versions, no matter what's in the remote repo
 * <br/>
 * com.braintribe.devrock.test.bias:bias-terminal#1.0.1
 * ->
 * com.braintribe.devrock.test.bias:a#1.0.2-pc
 * com.braintribe.devrock.test.bias:b#1.0.1
 * 
 * existing in remote repo are 
 * com.braintribe.devrock.test.bias:a#1.0.1
 * com.braintribe.devrock.test.bias:a#1.0.3
 * @author pit
 *
 */
public class ActiveBiasTestLab extends AbstractBiasLab {
		
	
	protected static File settings = new File( "res/biasLab/contents/settings.xml");
	protected static File bias = new File( "res/biasLab/contents/pc_bias.txt");
	
	@BeforeClass
	public static void before() {
		before( settings);
		TestUtil.copy(bias, localRepository, ".pc_bias");
	}


	@Override
	protected void testPresence(Collection<Solution> solutions, File repository) {
		super.testPresence(solutions, repository);
	}	
	
	@Test
	public void testActiveBias() {
		String[] expectedNames = new String [] {					
				"com.braintribe.devrock.test.bias:a#1.0.2-pc", // bias on this artifact -> not the highest version, but the highest pc 
				"com.braintribe.devrock.test.bias:b#1.0.1", // 					
		};
		
		runTest( "com.braintribe.devrock.test.bias:bias-terminal#1.0.1", expectedNames, ScopeKind.compile, WalkKind.classpath, false);
	}
	
	
	

	
}
