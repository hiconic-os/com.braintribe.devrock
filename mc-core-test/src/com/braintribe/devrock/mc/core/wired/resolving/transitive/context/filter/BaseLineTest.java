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

/**
 * simple baseline test to make sure that the initial content and its resolution are fine
 * 
 * @author pit
 *
 */
public class BaseLineTest extends AbstractResolvingContextTest {

	@Override
	protected RepoletContent archiveInput() {		
		return archiveInput( COMMON_CONTEXT_DEFINITION_YAML);
	}

	@Test
	public void noFilterOnTDR() {
		TransitiveResolutionContext trc = TransitiveResolutionContext.build().done();
		runAndValidate(trc, COMMON_CONTEXT_DEFINITION_YAML);
	}
	
	@Test
	public void noFilterOnCPR() {
		ClasspathResolutionContext trc = ClasspathResolutionContext.build().done();
		runAndValidate(trc, COMMON_CONTEXT_DEFINITION_YAML);
	}
}
