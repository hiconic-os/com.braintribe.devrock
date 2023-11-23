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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.repository.purge;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

/** 
 * tests mc-core's ArtifactRemoval - removes an artifact from a MavenFileSystemRepository (local-repo, install-repo)
 * 
 * @author pit
 *
 */
public class RepositoryInstallSelectivePurgeTest extends AbstractRepositoryPurgingTest  {	
	
	@Override
	protected File config() {	
		return new File( input, "repository-configuration.yaml");
	}
	
	/**
	 * simplest case : one single artifact with one single entry in the maven-metadata.xml
	 */
	@Test 
	public void testSingleArtifactRemoval() {
		VersionedArtifactIdentification vai = VersionedArtifactIdentification.create("com.braintribe.devrock.test", "a", "1.0.1-pc");		
		removeArtifact(vai, true);
	}

	/**
	 * a bit more complicated : one single artifact with multiple entries in the maven-metadata.xml 
	 */
	@Test 
	public void testSingleVersionedArtifactRemoval() {
		VersionedArtifactIdentification vai = VersionedArtifactIdentification.create("com.braintribe.devrock.test", "b", "1.0.1-pc");		
		removeArtifact(vai, false);
	}

	@Test 
	public void testMultipleArtifactRemoval() {
		Map<VersionedArtifactIdentification, Boolean> map = new HashMap<>();
		
		map.put( VersionedArtifactIdentification.create("com.braintribe.devrock.test", "a", "1.0.1-pc"), true);
		map.put( VersionedArtifactIdentification.create("com.braintribe.devrock.test", "b", "1.0.2-pc"), false);
		
		removeArtifacts(map);
	}
	
}
