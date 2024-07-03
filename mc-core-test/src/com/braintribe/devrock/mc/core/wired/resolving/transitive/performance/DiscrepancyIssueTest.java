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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.performance;

import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionScope;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
// deactivated test as terminal in question doesn't exist anymore. 
// pit, 22.11.2021

public class DiscrepancyIssueTest extends AbstractClasspathResolvingPerformanceTest {
	//@Test
	public void test() {

		TransitiveResolutionContext resolutionContextProvidedNoTestNoOptional = TransitiveResolutionContext.build().dependencyFilter( (d) -> !(d.getScope().equals("test") || d.getScope().equals( "provided") || d.getOptional())).done();
		TransitiveResolutionContext resolutionContextNoTest = TransitiveResolutionContext.build().dependencyFilter( (d) -> !(d.getScope().equals("test") || d.getOptional())).done();
		TransitiveResolutionContext resolutionContextProvided = TransitiveResolutionContext.build().dependencyFilter( (d) -> !d.getScope().equals( "provided")).done();
		TransitiveResolutionContext resolutionContextProvidedNoTest = TransitiveResolutionContext.build().dependencyFilter( (d) -> !(d.getScope().equals("test") || d.getScope().equals( "provided"))).done();
		TransitiveResolutionContext resolutionContextPlain = TransitiveResolutionContext.build().done();
		
		
		TransitiveResolutionContext resolutionContextExtract = TransitiveResolutionContext.build()
						.dependencyFilter( (d) -> !(d.getScope().equals("test") || d.getScope().equals( "provided") || d.getOptional()))
						.includeRelocationDependencies(true)
						.includeParentDependencies(true)
						.includeImportDependencies(true)
						.lenient(false)
						.done();
		
		
		// CPRContext for 'runtime'
		ClasspathResolutionContext classpathResolutionContext = ClasspathResolutionContext.build()
					.scope( ClasspathResolutionScope.runtime)
					.done();
		
		Pair<AnalysisArtifactResolution, Long> retval = resolveAsArtifact("tribefire.release:tribefire-release-deps#2.1.8", resolutionContextExtract);		
		AnalysisArtifactResolution resolution = retval.first;
		
		if (resolution.hasFailed()) {
			System.out.println( resolution.getFailure().asFormattedText());
		}
		else {
			System.out.println("no issued detected");
		}
		
		// count the numbers : 
		
		int s = resolution.getSolutions().size();
		System.out.println("Number of solutions : " + s);
	}

}
