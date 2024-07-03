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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.buildrange;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.transitive.BoundaryHit;
import com.braintribe.devrock.mc.api.transitive.BuildRange;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.testing.category.KnownIssue;


/**
 * a set of the tests that follow the example as declared in 'mc-core-documentation/src/
 * 
 * @author pit
 *
 */
public class ExampleDataBasedBuildRangeResolvingTest extends AbstractBuildRangeResolvingContextTest {
	private static final String EXAMPLE_DEFINITION_YAML = "tree.example.definition.yaml";
	protected static final String TERMINAL = "com.braintribe.terminal:Terminal#1.0.1";

	@Override
	protected RepoletContent archiveInput() {
		return archiveInput(EXAMPLE_DEFINITION_YAML);
	}
	
	@Test
	public void runTestWithoutBuildRange() {		
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().includeParentDependencies(true).buildRange( null).done();
		runAndValidate(TERMINAL, trc, "tree.example.validation.yaml");	
	}


	
	@Test
	//   com.braintribe.terminal:Terminal#1.0]
	public void runFirstExample() {
		HitExpert lowerExpert = new HitExpert();
		HitExpert upperExpert = new HitExpert( CompiledArtifactIdentification.parse(TERMINAL), BoundaryHit.closed);
		BuildRange range = BuildRange.of( lowerExpert::hit, upperExpert::hit);
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().includeParentDependencies(true).buildRange( range).done();
		runAndValidate(TERMINAL, trc, "tree.example.validation.yaml");
	}
	
	@Test
	//   com.braintribe.terminal:Terminal#1.0[
	public void runSecondExample() {
		HitExpert lowerExpert = new HitExpert();
		HitExpert upperExpert = new HitExpert( CompiledArtifactIdentification.parse(TERMINAL), BoundaryHit.open);
		BuildRange range = BuildRange.of( lowerExpert::hit, upperExpert::hit);
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().includeParentDependencies(true).buildRange( range).done();				
		runAndValidate(TERMINAL, trc, "second.example.validation.yaml");
	}
	
	
	@Test
	//  [com.braintribe.terminal:Terminal#1.0
	public void runThirdExample() {
		HitExpert lowerExpert = new HitExpert( CompiledArtifactIdentification.parse(TERMINAL), BoundaryHit.closed);
		HitExpert upperExpert = new HitExpert();
		BuildRange range = BuildRange.of( lowerExpert::hit, upperExpert::hit);
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().includeParentDependencies(true).buildRange( range).done();
		runAndValidate(TERMINAL, trc, "third.example.validation.yaml");
	}
	

	@Test
	//  ]com.braintribe.terminal:Terminal#1.0
	public void runFourthExample() {
		HitExpert lowerExpert = new HitExpert( CompiledArtifactIdentification.parse(TERMINAL), BoundaryHit.open);
		HitExpert upperExpert = new HitExpert();
		BuildRange range = BuildRange.of( lowerExpert::hit, upperExpert::hit);
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().includeParentDependencies(true).buildRange( range).done();
		runAndValidate(TERMINAL, trc, "empty.validation.yaml");
	}
	
	@Test
	//  [com.braintribe.terminal:Terminal#1.0]
	public void runFifthExample() {
		HitExpert lowerExpert = new HitExpert( CompiledArtifactIdentification.parse(TERMINAL), BoundaryHit.closed);
		HitExpert upperExpert = new HitExpert( CompiledArtifactIdentification.parse(TERMINAL), BoundaryHit.closed);
		BuildRange range = BuildRange.of( lowerExpert::hit, upperExpert::hit);
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().includeParentDependencies(true).buildRange( range).done();
		runAndValidate(TERMINAL, trc, "third.example.validation.yaml");
	}
	
	@Test
	//  ]com.braintribe.terminal:Terminal#1.0[
	public void runSixthExample() {
		HitExpert lowerExpert = new HitExpert( CompiledArtifactIdentification.parse(TERMINAL), BoundaryHit.open);
		HitExpert upperExpert = new HitExpert( CompiledArtifactIdentification.parse(TERMINAL), BoundaryHit.open);
		BuildRange range = BuildRange.of( lowerExpert::hit, upperExpert::hit);
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().includeParentDependencies(true).buildRange( range).done();
		runAndValidate(TERMINAL, trc, "empty.validation.yaml");
	}

	@Test
	@Category(KnownIssue.class)
	//TODO: doesn't work yet - is successfully compared against the full set, which should actually fail  
	//[com.braintribe.grpOne:C#1.0+com.braintribe.terminal:Terminal#1.0]
	public void runSeventhExample() {
		HitExpert upperExpert = new HitExpert( CompiledArtifactIdentification.parse(TERMINAL), BoundaryHit.closed);
		HitExpert lowerExpert = new HitExpert( CompiledArtifactIdentification.parse("com.braintribe.grpOne:C#1.0"), BoundaryHit.closed);
		BuildRange range = BuildRange.of( lowerExpert::hit, upperExpert::hit);
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().includeParentDependencies(true).buildRange( range).done();
		runAndValidate(TERMINAL, trc, "seventh.example.validation.yaml");
	}
	
	// @Test
	@Category(KnownIssue.class)
	//TODO: doesn't work yet
	//[com.braintribe.grpOne:C#1.0]+[com.braintribe.terminal:Terminal#1.0]
	public void runEighthExample() {
		HitExpert lowerExpert = new HitExpert( 
										Pair.of( CompiledArtifactIdentification.parse(TERMINAL), BoundaryHit.closed), 
										Pair.of( CompiledArtifactIdentification.parse("com.braintribe.grpOne:C#1.0"), BoundaryHit.closed)
								);
										
		HitExpert upperExpert =  new HitExpert( 
				Pair.of( CompiledArtifactIdentification.parse(TERMINAL), BoundaryHit.closed), 
				Pair.of( CompiledArtifactIdentification.parse("com.braintribe.grpOne:C#1.0"), BoundaryHit.closed)
		);
		BuildRange range = BuildRange.of( lowerExpert::hit, upperExpert::hit);
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().includeParentDependencies(true).buildRange( range).done();
		runAndValidate(TERMINAL, trc, "eighth.example.validation.yaml");
	}
	@Test
	@Category(KnownIssue.class)
	//TODO: doesn't work yet - is sucessfully compared against the full set, which should actually fail
	//[com.braintribe.grpOne.subOne:B#1.0+[com.braintribe.grpOne:C#1.0+com.braintribe.terminal:Terminal#1.0]
	public void runNinthExample() {
		HitExpert lowerExpert = new HitExpert( 
											Pair.of( CompiledArtifactIdentification.parse("com.braintribe.grpOne:B#1.0"), BoundaryHit.closed),
											Pair.of( CompiledArtifactIdentification.parse("com.braintribe.grpOne:C#1.0"), BoundaryHit.closed)
							    );
		HitExpert upperExpert = new HitExpert( CompiledArtifactIdentification.parse(TERMINAL), BoundaryHit.closed);
		BuildRange range = BuildRange.of( lowerExpert::hit, upperExpert::hit);
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().includeParentDependencies(true).buildRange( range).done();
		runAndValidate(TERMINAL, trc, "nineth.example.validation.yaml");
	}			
		
}
