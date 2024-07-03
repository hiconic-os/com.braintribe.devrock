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
package com.braintribe.test.multi.settingsVariationLab;

import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.KnownIssue;

@Category(KnownIssue.class)
public class NoSettingsVariationLab extends AbstractSettingsVariationLab {
	
	@BeforeClass
	public static void before() {
		before( null);
		virtualEnvironment.addPropertyOverride("user.home", contents.getAbsolutePath());
		virtualEnvironment.addEnvironmentOverride( "M2_REPO", "");
		virtualEnvironment.addEnvironmentOverride( "M2_HOME", "");
		
	}
	@Override
	protected String[] getResultsForRun() {	
		String [] result = new String[2];
		result [0] = "com.braintribe.test.dependencies.repositoryRoleTest:A#1.0";
		result [1] = "com.braintribe.test.dependencies.repositoryRoleTest:B#1.0";
		return result;
	}

}
