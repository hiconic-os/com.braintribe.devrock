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
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.junit.Test;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.ClashResolvingStrategy;

public class TestOnMalaclypse extends AbstractClasspathResolvingPerformanceTest {

	// @Test
	public void singleRunTest() {
		Pair<AnalysisArtifactResolution, Long> retval = resolve("com.braintribe.devrock:malaclypse#[1.0,1.1)", resolutionContext());
		
		System.out.println( retval.first.hasFailed() ? "failed" : "successful" + " resolution took [" + retval.second / 1_000_000_000D + "s]");
	}


	private ClasspathResolutionContext resolutionContext() {
		return ClasspathResolutionContext.build().clashResolvingStrategy(ClashResolvingStrategy.highestVersion).enrichJavadoc(true).enrichSources(true).done();
	}
	

	// @Test
	public void cachedRunTest() {
		Pair<AnalysisArtifactResolution, Long> retval1 = resolve("com.braintribe.devrock:malaclypse#[1.0,1.1)", resolutionContext());
		System.out.println( retval1.first.hasFailed() ? "failed" : "successful" + " resolution took (empty cache) [" + retval1.second / 1E6 + " ms]");
		
		reinitialize();
		Pair<AnalysisArtifactResolution, Long> retval2 = resolve("com.braintribe.devrock:malaclypse#[1.0,1.1)", resolutionContext());
		System.out.println( retval2.first.hasFailed() ? "failed" : "successful" + " resolution took (filesystem cache) [" + retval2.second / 1E6 + " ms]");
		
		Pair<AnalysisArtifactResolution, Long> retval3 = resolve("com.braintribe.devrock:malaclypse#[1.0,1.1)", resolutionContext());
		System.out.println( retval3.first.hasFailed() ? "failed" : "successful" + " resolution (full cache) took [" + retval3.second / 1E6 + " ms]");
	}
	
	
	//@Test
	public void dump() {
		Pair<AnalysisArtifactResolution, Long> retval = resolve("com.braintribe.devrock:malaclypse#[1.0,1.1)", resolutionContext());
		YamlMarshaller marshaller = new YamlMarshaller();
		marshaller.setWritePooled(true);
		
		try (OutputStream out = new FileOutputStream( new File( output, "malaclypse-legacy.dump.yaml"))){
			marshaller.marshall(out, retval.first);		
		}
		catch (Exception e) {
			;
		}
	}
	

}
