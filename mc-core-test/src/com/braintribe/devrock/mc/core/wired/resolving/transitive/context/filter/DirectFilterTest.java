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
