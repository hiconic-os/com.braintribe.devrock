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
import com.braintribe.devrock.mc.api.transitive.ArtifactPathElement;
import com.braintribe.devrock.mc.api.transitive.DependencyPathElement;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.model.repolet.content.RepoletContent;

/**
 * tests path filtering, i.e. filters that take the traversed tree into account.
 * path filtering on artifacts is currently only supported by TDR, currently CPR only supports
 * dependency path filtering. 
 *  
 * @author pit
 *
 */

// TODO: eventually, if decided, add test for artifact path filtering on CPR
public class PathFilterTest extends AbstractResolvingContextTest {
	 

	@Override
	protected RepoletContent archiveInput() {		
		return archiveInput( "common.context.definition.yaml");
	}
	
	/**
	 * filters out the dependency 'z' from the artifact 'y'
	 * @param dpe 
	 * @return
	 */
	private boolean filter( DependencyPathElement dpe) {
		if (
				dpe.getDependency().getArtifactId().equals("z") &&
				dpe.getParent().getArtifact().getArtifactId().equals("y")
			)
			return false;
		return true;
	}
	

	/**
	 * filters out the dependency 'z' from the artifact 'y'
	 * @param ape
	 * @return
	 */
	private boolean filter( ArtifactPathElement ape) {
		if (
				ape.getArtifact().getArtifactId().equals("z") &&
				ape.getParent().getDependency().getArtifactId().equals("z") &&
				ape.getParent().getParent().getArtifact().getArtifactId().equals("y")
			)
			return false;
		return true;
	}
		

	@Test
	public void dependencyPathFilterOnTDR() {		
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().dependencyPathFilter( this::filter).done();
		runAndValidate(trc, "dependencyPathFilter.context.validation.yaml");
	}
	
	@Test
	public void artifactPathFilterOnTDR() {		
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().artifactPathFilter( this::filter).done();
		runAndValidate(trc, "dependencyPathFilter.context.validation.yaml");
	}

	@Test
	public void dependencyPathFilterOnCPR() {		
		ClasspathResolutionContext trc = ClasspathResolutionContext.build().dependencyPathFilter( this::filter).done();
		runAndValidate(trc, "dependencyPathFilter.context.validation.yaml");
	}
	
	@Test
	public void artifactPathFilterOnCPR() {		
		ClasspathResolutionContext trc = ClasspathResolutionContext.build().artifactPathFilter( this::filter).done();
		runAndValidate(trc, "dependencyPathFilter.context.validation.yaml");
	}
		

}
