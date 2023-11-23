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
