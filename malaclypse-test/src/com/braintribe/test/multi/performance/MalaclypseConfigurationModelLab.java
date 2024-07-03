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
package com.braintribe.test.multi.performance;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.Slow;


/**
 
 * 
 * @author pit
 *
 */
@Category(Slow.class)
public class MalaclypseConfigurationModelLab extends AbstractPerformanceLab {
		
	private static final String TERMINAL = "com.braintribe.devrock:malaclypse-plugin-configuration-model#[1.0,1.1)";	

	@BeforeClass
	public static void before() {
		File settings = determineSettings();
		before( settings);
	}

	
	//@Test
	public void runMalaclypseConfigurationModel() {
		run( TERMINAL);
	}
	
	
	

	
}
