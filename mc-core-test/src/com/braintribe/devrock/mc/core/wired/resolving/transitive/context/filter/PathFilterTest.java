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
