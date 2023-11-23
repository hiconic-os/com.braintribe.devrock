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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.buildrange;

import org.junit.Test;

import com.braintribe.devrock.mc.api.transitive.BoundaryHit;
import com.braintribe.devrock.mc.api.transitive.BuildRange;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;

/**
 * tests TDR's build range feature on a very simple tree - only linear dependencies,
 * i.e. t -> a -> b -> x -> y -> z
 *  
 *  shows : 
 *  all tests referencing ranges WITHIN the tree, i.e NO start or end of tree included, work.
 *  however, if one of the end points (start or end) are included, the result is not what is intended.
 *  
 * @author pit
 *
 */
public class SimplestTreeBuildRangeResolvingTest extends AbstractBuildRangeResolvingContextTest {
	protected static final String SIMPLEST_DEFINITION_YAML = "simplest.tree.definition.yaml";
	protected static final String GROUP = "com.braintribe.devrock.test";
	protected static final String VERSION = "1.0.1";
	protected static final String TERMINAL = GROUP + ":t#" + VERSION;
	
	@Override
	protected RepoletContent archiveInput() {
		return archiveInput(SIMPLEST_DEFINITION_YAML);
	}
	
	/**
	 * tests that without ranges, the input tree is returned
	 */
	@Test
	public void runTestWithoutBuildRange() {		
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().buildRange( null).done();
		runAndValidate(TERMINAL, trc, SIMPLEST_DEFINITION_YAML);
	}
	
	@Test 
	public void runTestBuildRangeOnTerminal() {
		HitExpert lowerExpert = new HitExpert();
		HitExpert upperExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":t#"+ VERSION), BoundaryHit.closed);
		BuildRange buildRange = BuildRange.of( lowerExpert::hit, upperExpert::hit);
		
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().buildRange( buildRange).done();
		runAndValidate(TERMINAL, trc, SIMPLEST_DEFINITION_YAML);		
		
	}
	
	@Test 
	public void runTestBuildRangeOnTerminal_Open() {
		HitExpert lowerExpert = new HitExpert();		
		HitExpert upperExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":t#"+ VERSION), BoundaryHit.open);
		BuildRange buildRange = BuildRange.of( lowerExpert::hit, upperExpert::hit);
		
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().buildRange( buildRange).done();	
		runAndValidate(TERMINAL, trc, "std.a.validation.yaml");
		
	}
	
	
	/**
	 * test on closed interval of a -> b  -> returns b, a
	 */
	@Test
	public void runTestBuildRangeOnIntervalOfAToB() {
		HitExpert lowerExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":b#"+ VERSION), BoundaryHit.closed);
		HitExpert upperExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":a#"+ VERSION), BoundaryHit.closed);
		BuildRange buildRange = BuildRange.of( lowerExpert::hit, upperExpert::hit);
		
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().buildRange( buildRange).done();
		runAndValidate(TERMINAL, trc, "std.a2b.validation.yaml");
	}
	
	/**
	 * test on closed interval of b -> x -> returns x, b 
	 */
	@Test
	public void runTestBuildRangeOnOnIntervalOfBToX() {
		HitExpert lowerExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":x#"+ VERSION), BoundaryHit.closed);
		HitExpert upperExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":b#"+ VERSION), BoundaryHit.closed);
		BuildRange buildRange = BuildRange.of( lowerExpert::hit, upperExpert::hit);
				
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().buildRange( buildRange).done();
		runAndValidate(TERMINAL, trc, "std.b2x.validation.yaml");
	}
	
	/**
	 * test on closed interval of x -> z, returns z, y, x
	 */
	@Test
	public void runTestBuildRangeOnOnIntervalOfXToZ() {
		HitExpert lowerExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":z#"+ VERSION), BoundaryHit.closed);
		HitExpert upperExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":x#"+ VERSION), BoundaryHit.closed);
		BuildRange buildRange = BuildRange.of( lowerExpert::hit, upperExpert::hit);

		
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().buildRange( buildRange).done();
		runAndValidate(TERMINAL, trc, "std.x2z.validation.yaml");	
	}
	
	/**
	 * test of open interval of x -> z, should only return y
	 */
	@Test
	public void runTestBuildRangeOnOnIntervalOfXToZ_Open() {
		HitExpert lowerExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":z#"+ VERSION), BoundaryHit.open);
		HitExpert upperExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":x#"+ VERSION), BoundaryHit.open);
		BuildRange buildRange = BuildRange.of( lowerExpert::hit, upperExpert::hit);

		
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().buildRange( buildRange).done();
		runAndValidate(TERMINAL, trc, "std.x2z.open.validation.yaml");
	}
	

	@Test
	public void runTestBuildRangeOnXToEndOfTree_Open() {
		HitExpert lowerExpert = new HitExpert();
		HitExpert upperExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":x#"+ VERSION), BoundaryHit.open);
		BuildRange buildRange = BuildRange.of( lowerExpert::hit, upperExpert::hit);

		
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().buildRange( buildRange).done();
		runAndValidate(TERMINAL, trc, "std.y2z.open.validation.yaml");
	}

	
}
