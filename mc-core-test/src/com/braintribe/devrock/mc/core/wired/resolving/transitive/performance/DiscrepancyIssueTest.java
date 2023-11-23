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
