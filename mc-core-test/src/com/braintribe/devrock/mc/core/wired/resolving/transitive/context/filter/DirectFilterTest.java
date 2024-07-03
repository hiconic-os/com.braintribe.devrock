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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.context.filter;

import org.junit.Test;

import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisDependency;

/**
 * tests direct filtering on dependency and artifact.
 * TDR supports both, CPR only supports filtering dependencies
 * 
 * @author pit
 *
 */
public class DirectFilterTest extends AbstractResolvingContextTest {
	
	@Override
	protected RepoletContent archiveInput() {		
		return archiveInput( "common.context.definition.yaml");
	}
	
	/**
	 * filters out any dependency 'z' 
	 * @param ad 
	 * @return
	 */
	private boolean filter( AnalysisDependency ad) {		
		return !ad.getArtifactId().equals("z");
	}


	/**
	 * filters out any artifact 'z'
	 * @param aa
	 * @return
	 */
	private boolean filter( AnalysisArtifact aa) {
		return !aa.getArtifactId().equals("z");
	}
		

	@Test
	public void dependencyFilterOnTDR() {		
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().dependencyFilter( this::filter).done();
		runAndValidate(trc, "directFilter.context.validation.yaml");
	}
	
	@Test
	public void dependencyFilterOnCPR() {		
		ClasspathResolutionContext crc = ClasspathResolutionContext.build().filterDependencies( this::filter).done();
		runAndValidate(crc, "directFilter.context.validation.yaml");
	}
	
	@Test
	public void artifactFilterOnTDR() {		
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().artifactFilter( this::filter).done();
		runAndValidate(trc, "directFilter.context.validation.yaml");
	}
	

}
