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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.bias.dominance;

import java.io.File;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.testing.category.KnownIssue;

/**
 * tests a dominance issue found in mc-core..
 * current state in mc-core#1.0.103 is that a dominant repository blocks any further lookup if it creates ANY content, 
 * and not only if it creates MEANINGFUL content
 * i.e. dominant : 1.1, recessive 1.2, 1.3
 * if asked for [1.1,1.3) -> 1.1 needs to be returned (works also in #1.0.103) 
 * if asked for [1.3,1.5) -> 1.3 needs to be returned (fails in #1.0.103)
 * if asked for [1.0,2.0) -> 1.1 needs to be returned
 * @author pit
 *
 */
@Category( KnownIssue.class)
public class DominanceTest extends AbstractTransitiveResolverDominanceTest {
	
	/**
	 * tests the case where the dominant result is within the requested range 
	 */
	@Test
	public void testDominanceWithinRange() {
		AnalysisArtifactResolution resolution = run(terminalOne, standardResolutionContext);
		Validator validator = new Validator();
		validator.validate( new File( input, "dominance.within.range.validation.yaml"), resolution);
		validator.assertResults();
	}
	
	/**
	 * tests the case where the dominant result is outside the requested range
	 */
	@Test
	public void testDominanceOutsideRange() {
		AnalysisArtifactResolution resolution = run(terminalTwo, standardResolutionContext);
		Validator validator = new Validator();
		validator.validate( new File( input, "dominance.outside.range.validation.yaml"), resolution);
		validator.assertResults();
	}
	
	/**
	 * tests the case where a lower version is the dominant repository, and the higher versions are 
	 * in the recessive repositories
	 */
	@Test
	public void testDominanceAcrossRange() {
		AnalysisArtifactResolution resolution = run(terminalThree, standardResolutionContext);
		Validator validator = new Validator();
		validator.validate( new File( input, "dominance.across.range.validation.yaml"), resolution);
		validator.assertResults();
	}
	
}
