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

import java.io.File;

import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionScope;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

public class BrokenResolutionLab extends AbstractClasspathResolvingPerformanceTest {
	// switch here for different settings 
	private File currentSettings = standardSettings; // adxSettings or standardSettings


	@Override
	protected String getSettingsPath() {
		return currentSettings.getAbsolutePath();
	}

	//@Test
	public void testIssue() {
		//Pair<AnalysisArtifactResolution, Long> retval = run("tribefire.cortex:platform-reflection-model#[2.0,2.1)", ClasspathResolutionContext.build().clashResolvingStrategy(ClashResolvingStrategy.highestVersion).done());
	
		//Pair<AnalysisArtifactResolution, Long> retval = run("tribefire.cortex:parent#[2.0,2.1)", ClasspathResolutionContext.build().clashResolvingStrategy(ClashResolvingStrategy.highestVersion).done());
	
		
		//"tribefire.extension.aws:aws-module#2.3.1"
		
		//CompiledArtifact ca = resolve( "tribefire.cortex:platform-reflection-model#[2.0,2.1)");

		// TDRContext for 'runtime' 
		TransitiveResolutionContext resolutionContext = TransitiveResolutionContext.build().dependencyFilter( (d) -> !(d.getScope().equals("test") || d.getScope().equals( "provided") || d.getOptional())).done();
		
		// CPRContext for 'runtime'
		ClasspathResolutionContext classpathResolutionContext = ClasspathResolutionContext.build()
					.scope( ClasspathResolutionScope.runtime)
					.done();
		
		//Pair<AnalysisArtifactResolution, Long> retval = resolveAsArtifact("tribefire.cortex.services:tribefire-web-platform#2.0.245", resolutionContext);
		Pair<AnalysisArtifactResolution, Long> retval = resolveAsArtifact("tribefire.cortex:jdbc-dcsa-storage-plugin#2.0.1-pc", resolutionContext);
		AnalysisArtifactResolution resolution = retval.first;
		
		if (resolution.hasFailed()) {
			System.out.println( resolution.getFailure().asFormattedText());
		}
		else {
			System.out.println("no issued detected");
		}
		
		
	}
}
