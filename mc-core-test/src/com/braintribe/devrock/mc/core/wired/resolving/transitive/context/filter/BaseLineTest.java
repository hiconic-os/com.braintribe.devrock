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
