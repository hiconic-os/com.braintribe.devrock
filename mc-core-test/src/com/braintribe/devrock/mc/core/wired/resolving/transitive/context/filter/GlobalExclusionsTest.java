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

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionScope;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.model.artifact.essential.ArtifactIdentification;

/**
 * tests global exclusions - not via pom, but via the context
 * 
 * @author pit
 *
 */

public class GlobalExclusionsTest extends AbstractResolvingContextTest {

	@Override
	protected RepoletContent archiveInput() {		
		return archiveInput( COMMON_CONTEXT_DEFINITION_YAML);
	}
	
	/**
	 * test run with the TDR
	 */
	@Test
	public void runGlobalExclusionPerContextTest() {
		// remove a-1, b-1
		Set<ArtifactIdentification> exclusions = new HashSet<>();
		exclusions.add( ArtifactIdentification.create( "com.braintribe.devrock.test", "a-1"));
		exclusions.add( ArtifactIdentification.create( "com.braintribe.devrock.test", "b-1"));
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().globalExclusions(exclusions).done();
		runAndValidate(trc, "filtered.context.validation.yaml");
	}
	
	/**
	 * test run with CPR
	 */
	@Test
	public void runGlobalExclusionPerContextCprTest() {
		// remove a-1, b-1
		Set<ArtifactIdentification> exclusions = new HashSet<>();
		exclusions.add( ArtifactIdentification.create( "com.braintribe.devrock.test", "a-1"));
		exclusions.add( ArtifactIdentification.create( "com.braintribe.devrock.test", "b-1"));
		ClasspathResolutionContext trc = ClasspathResolutionContext.build().scope(ClasspathResolutionScope.compile).globalExclusions(exclusions).done();
		runAndValidate(trc, "filtered.context.validation.yaml");
	}


}
