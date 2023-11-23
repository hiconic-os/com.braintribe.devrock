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
