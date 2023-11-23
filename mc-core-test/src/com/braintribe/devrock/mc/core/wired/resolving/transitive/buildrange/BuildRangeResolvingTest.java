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
 * tests TDR's build range feature 
 * @author pit
 *
 */
public class BuildRangeResolvingTest extends AbstractBuildRangeResolvingContextTest {
	protected static final String DEFINITION_YAML = "tree.definition.yaml";
	protected static final String GROUP = "com.braintribe.devrock.test";
	protected static final String VERSION = "1.0.1";
	protected static final String TERMINAL = GROUP + ":t#" + VERSION;
	
	@Override
	protected RepoletContent archiveInput() {
		return archiveInput(DEFINITION_YAML);
	}
	
	@Test
	public void runTestWithoutBuildRange() {		
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().buildRange( null).done();
		runAndValidate(TERMINAL, trc, DEFINITION_YAML);
	}
	
	@Test
	public void runTestBuildRangeOnTerminal() {
		//HitExpert lowerExpert = new HitExpert(null, BoundaryHit.none);
		HitExpert lowerExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":z#"+ VERSION), BoundaryHit.closed);
		HitExpert upperExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":t#"+ VERSION), BoundaryHit.closed);
		BuildRange buildRange = BuildRange.of( lowerExpert::hit, upperExpert::hit);
		
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().buildRange( buildRange).done();
		runAndValidate(TERMINAL, trc, DEFINITION_YAML);		
	}
	
	
	@Test
	// TODO : check.. can't explain directly the result 
	public void runTestBuildRangeOnIntervalAToB() {
		HitExpert lowerExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":b#"+ VERSION), BoundaryHit.closed);
		HitExpert upperExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":a#"+ VERSION), BoundaryHit.closed);
		BuildRange buildRange = BuildRange.of( lowerExpert::hit, upperExpert::hit);
		
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().buildRange( buildRange).done();	
		//runAndValidate(TERMINAL, trc, "c.a2b.validation.yaml");
		runAndShow(TERMINAL, trc);
	}
	
	@Test
	// TODO: check ... can't explain directly the result
	public void runTestBuildRangeOnIntervalAToC() {
		HitExpert lowerExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":c#"+ VERSION), BoundaryHit.open);
		HitExpert upperExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":a#"+ VERSION), BoundaryHit.open);
		BuildRange buildRange = BuildRange.of( lowerExpert::hit, upperExpert::hit);
		
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().buildRange( buildRange).done();		
		//runAndValidate(TERMINAL, trc, "c.a2c.validation.yaml");
		runAndShow(TERMINAL, trc);
	}
	
	@Test
	public void runTestBuildRangeOnIntervalAToCommon() {
		HitExpert lowerExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":common#"+ VERSION), BoundaryHit.closed);
		HitExpert upperExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":a#"+ VERSION), BoundaryHit.closed);
		BuildRange buildRange = BuildRange.of( lowerExpert::hit, upperExpert::hit);
		
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().buildRange( buildRange).done();
		runAndValidate(TERMINAL, trc, "c.a2common.validation.yaml");
	}
	
	@Test
	public void runTestBuildRangeOnIntervalTToCommon() {
		HitExpert lowerExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":common#"+ VERSION), BoundaryHit.closed);
		HitExpert upperExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":t#"+ VERSION), BoundaryHit.closed);
		BuildRange buildRange = BuildRange.of( lowerExpert::hit, upperExpert::hit);
		
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().buildRange( buildRange).done();
		runAndValidate(TERMINAL, trc, "c.t2common.validation.yaml");
	}
	
	
	@Test
	//TODO : check  
	// closed : returns t -> a -> c -> common -> x -> y -> z ... if boundary on common is closed.. why?
	// open : returns x -> y -> z  .. if boundary on common is open.
	//
	// expectation: open is correct, closed should return common -> x -> y -> z ??  
	
	public void runTestBuildRangeOnIntervalCommonToZ() {
		HitExpert lowerExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":z#"+ VERSION), BoundaryHit.closed);
		HitExpert upperExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":common#"+ VERSION), BoundaryHit.closed);
		BuildRange buildRange = BuildRange.of( lowerExpert::hit, upperExpert::hit);
				
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().buildRange( buildRange).done();
		//runAndValidate(TERMINAL, trc, "buildRange.b.validation.yaml");
		runAndShow(TERMINAL, trc);
	}
	

	@Test
	public void runTestBuildRangeOnXes() {
		HitExpert lowerExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":z#"+ VERSION), BoundaryHit.closed);
		HitExpert upperExpert = new HitExpert(CompiledArtifactIdentification.parse( GROUP + ":x#"+ VERSION), BoundaryHit.closed);
		BuildRange buildRange = BuildRange.of( lowerExpert::hit, upperExpert::hit);

		
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().buildRange( buildRange).done();
		runAndValidate(TERMINAL, trc, "buildRange.x.validation.yaml");
		//runAndShow(TERMINAL, trc);
		
	}
	
}
