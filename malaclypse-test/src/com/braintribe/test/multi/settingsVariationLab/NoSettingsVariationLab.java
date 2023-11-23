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
