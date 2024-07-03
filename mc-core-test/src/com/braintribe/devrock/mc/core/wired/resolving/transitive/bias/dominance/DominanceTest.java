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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.bias.dominance;

import java.io.File;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.testing.category.KnownIssue;

/**
 * tests a dominance issue found in mc-core..
 * 	archive content : 
 * 		terminal#1.0.1 -> b#[1.0,1.3)
 * 		terminal#1.0.2 -> b#[1.3, 1.5)
 * 		terminal#1.0.3 -> b#[1.0, 2.0)
 * 
 * dominant content
 *  	b#1.1
 * recessive content
 * 		b#1.2
 * 		b#1.3
 * 
 * current state in mc-core#1.0.103 is that a dominant repository blocks any further lookup if it creates ANY content, 
 * and not only if it creates MEANINGFUL content
 * 
 * i.e. dominant : b#1.1, recessive b#1.2, b#1.3
 * 
 * if asked for [1.1,1.3) -> 1.1 needs to be returned (works also in #1.0.103) 
 * if asked for [1.3,1.5) -> 1.3 needs to be returned (fails in #1.0.103)
 * if asked for [1.0,2.0) -> 1.1 needs to be returned
 * 
 * @author pit
 *
 */
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
