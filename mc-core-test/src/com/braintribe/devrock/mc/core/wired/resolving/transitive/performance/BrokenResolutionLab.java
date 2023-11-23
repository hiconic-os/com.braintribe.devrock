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
