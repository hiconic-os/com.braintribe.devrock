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
package com.braintribe.devrock.mc.core.repository.resolving;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.devrock.mc.api.commons.VersionInfo;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.resolver.BasicVersionInfo;
import com.braintribe.devrock.mc.core.resolver.LocalRepositoryCachingArtifactResolver;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;

/**
 * tests the functionality on the local repository only (no delegates)
 * @author pit
 *
 */
public class LocalResolvingTest extends AbstractLocalRepositoryCachingArtifactResolverTest {	
	private String artifact = "com.braintribe.devrock.test:artifact#1.0";		
	
	@Override
	protected String getRoot() {	
		return "localArtifactPartResolving";
	}


	@Before
	public void before() {
		TestUtils.ensure( repo);		
		TestUtils.copy( new File(input, "repo"), repo);		
	}
	

	/**
	 * run a version-resolving test with only the basic local resolver (local repository, i.e. repoid == "local")
	 */
	@Test
	public void testVersionResolving() {
		CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse(artifact);
		LocalRepositoryCachingArtifactResolver resolver = setup(Collections.emptyList());
		List<VersionInfo> expected = new ArrayList<>();
		expected.add( new BasicVersionInfo( cai.getVersion(), Collections.singletonList("local")));
		
		testVersionInfoResolving( resolver, cai, expected);		
	}
	
	
	/**
	 * run a part-resolving test with only the basic local resolver (local repository, i.e. repoid == "local")
	 */
	@Test
	public void testPartResolving() {		
		CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse(artifact);
		LocalRepositoryCachingArtifactResolver resolver = setup(Collections.emptyList());		
		testPartResolving( resolver, cai, standardParts);				
	}

	
}
