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
package com.braintribe.test.processors;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;

/**
 * testing the comparison feature of the {@link ArtifactProcessor}
 * @author pit
 *
 */
public class ArtifactProcessorLab {
	
	private Dependency dep1 = NameParser.parseCondensedDependencyName( "a.b.c:xyz#1.0");	
	private Dependency dep1_2 = NameParser.parseCondensedDependencyName( "a.b.c:xyz#1.0");
	private Dependency dep1x = NameParser.parseCondensedDependencyName( "a.b.c:xyz#1.1");
	
	private Dependency dep2 = NameParser.parseCondensedDependencyName( "a.b.c:xyz#[1.0,1.1)");
	private Dependency dep2_2 = NameParser.parseCondensedDependencyName( "a.b.c:xyz#[1.0,1.1)");		
	private Dependency dep2x = NameParser.parseCondensedDependencyName( "a.b.c:xyz#[1.1,1.2)");
	
	private Artifact art1 = NameParser.parseCondensedArtifactName( "a.b.c:xyz#1.0");
	private Artifact art1_2 = NameParser.parseCondensedArtifactName( "a.b.c:xyz#1.0");
	private Artifact art2 = NameParser.parseCondensedArtifactName( "a.b.c:xyz#1.1");
	
	
	/**
	 * test dependencies
	 */
	@Test
	public void testDependencies() {
		int compare = ArtifactProcessor.compare(dep1, dep1);
		Assert.assertTrue( "expected [0], but found [" + compare + "]", compare==0);
		
		compare = ArtifactProcessor.compare(dep1, dep1_2);
		Assert.assertTrue( "expected [0], but found [" + compare + "]", compare==0);
		
		compare = ArtifactProcessor.compare(dep1, dep1x);
		Assert.assertTrue( "expected [-1], but found [" + compare + "]", compare==-1);
		
		compare = ArtifactProcessor.compare(dep1x, dep1);
		Assert.assertTrue( "expected [1], but found [" + compare + "]", compare==1);
		
		compare = ArtifactProcessor.compare(dep2, dep2);
		Assert.assertTrue( "expected [0], but found [" + compare + "]", compare==0);
		
		compare = ArtifactProcessor.compare(dep2, dep2_2);
		Assert.assertTrue( "expected [0], but found [" + compare + "]", compare==0);
		
		compare = ArtifactProcessor.compare(dep2, dep2x);
		Assert.assertTrue( "expected [-1], but found [" + compare + "]", compare==-1);
		
		compare = ArtifactProcessor.compare(dep2x, dep2);
		Assert.assertTrue( "expected [1], but found [" + compare + "]", compare==1);
	}

	/**
	 * test artifacts 
	 */
	@Test
	public void testArtifacts() {
		int compare = ArtifactProcessor.compare(art1, art2);
		Assert.assertTrue( "expected [-1], but found [" + compare + "]", compare==-1);
		compare = ArtifactProcessor.compare(art1, art1_2);
		Assert.assertTrue( "expected [0], but found [" + compare + "]", compare==0);
		compare = ArtifactProcessor.compare(art2, art1);
		Assert.assertTrue( "expected [1], but found [" + compare + "]", compare==1);
	}
}
