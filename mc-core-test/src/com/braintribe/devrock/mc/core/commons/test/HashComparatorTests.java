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
package com.braintribe.devrock.mc.core.commons.test;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.essential.ArtifactIdentification;

/**
 * just some simple test to see that the pre-packaged HashingComparators do work
 * @author pit
 *
 */
public class HashComparatorTests {

	/**
	 * test {@link ArtifactIdentification}, groupId & artifactId
	 */
	@Test
	public void artifactIdentificationTest() {
		// set 
		Set<ArtifactIdentification> set = HashComparators.artifactIdentification.newHashSet();
		
		ArtifactIdentification ai1 = ArtifactIdentification.parse("my.group.one:my-artifact-one");
		set.add(ai1);
		
		ArtifactIdentification ai2 = ArtifactIdentification.parse("my.group.two:my-artifact-one");
		set.add(ai2);
		
		assertTrue( "expected size is [2], found [" + set.size() + "]",set.size() == 2);
		
		ArtifactIdentification ai3 = ArtifactIdentification.parse("my.group.one:my-artifact-one");
		set.add(ai3);
		
		assertTrue( "expected size is [2], found [" + set.size() + "]",set.size() == 2);
						
	}
	
	/**
	 * test comparator on {@link CompiledArtifactIdentification}, groupId, artifactId and version 
	 */
	@Test
	public void compiledArtifactIdentificationTest() {
		// set 
		Set<CompiledArtifactIdentification> set = HashComparators.compiledArtifactIdentification.newHashSet();
		
		CompiledArtifactIdentification ai1 = CompiledArtifactIdentification.parse("my.group.one:my-artifact-one#1.0");
		set.add(ai1);
		
		CompiledArtifactIdentification ai2 = CompiledArtifactIdentification.parse("my.group.two:my-artifact-one#1.0");
		set.add(ai2);
		
		assertTrue( "expected size is [2], found [" + set.size() + "]",set.size() == 2);
		
		CompiledArtifactIdentification ai3 = CompiledArtifactIdentification.parse("my.group.one:my-artifact-one#1.0");
		set.add(ai3);
		
		assertTrue( "expected size is [2], found [" + set.size() + "]",set.size() == 2);
						
	}


}
