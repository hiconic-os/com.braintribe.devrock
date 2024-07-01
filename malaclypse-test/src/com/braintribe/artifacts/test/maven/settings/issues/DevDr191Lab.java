// ============================================================================
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
package com.braintribe.artifacts.test.maven.settings.issues;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.model.malaclypse.cfg.repository.RemoteRepository;

public class DevDr191Lab extends AbstractMavenSettingsLab{
	private static File contents = new File( "res/maven/settings/issues");

	private static File settings = new File( contents, "settings.devdr.191.xml");
	private static File localRepository = new File ( contents, "repo");


	
	
	/*
	 * RUL: the environment for overrides is hand-crafted !
	 */
	@BeforeClass
	public static void before() {
		before(settings, localRepository);
		//ove.addEnvironmentOverride("PROFILE_USECASE", "CORE");
		ove.addEnvironmentOverride( "CUSTOM_TARGET_REPO", "CUSTOM");
		ove.addEnvironmentOverride("DEVROCK_TESTS_REPOSITORY_BASE_URL", "https://blubb");
		ove.addEnvironmentOverride("DEVROCK_TESTS_READ_USERNAME", "blubb");
		ove.addEnvironmentOverride("DEVROCK_TESTS_READ_PASSWORD", "blubb");
		ove.addEnvironmentOverride("DEVROCK_TESTS_RAVENHURST_BASE_URL", "https://blubb");
	}

	
	@Test
	public void test() {
		List<RemoteRepository> allRemoteRepositories = getReader().getAllRemoteRepositories();
		allRemoteRepositories.stream().forEach( r -> System.out.println( r.getUrl()));
	}

}
