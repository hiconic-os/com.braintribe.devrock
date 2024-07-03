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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.extraction;

import java.util.List;
import java.util.function.Predicate;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.testing.category.KnownIssue;

/**
 * little 'exemplary' test for a full-fledged repository extract.  
 * 
 * currently deactivated as the used version of the test terminal are frequently removed and hence impossible to resolve
 * if needed, make sure the replace the versions of the terminal with one that really exists. 
 * @author pit
 *
 */
@Category( KnownIssue.class)
public class RepositoryExtractTest extends AbstractRepositoryExtractionTest {
	
	private static final String TEST_TERMINAL = "tribefire.cortex.services:tribefire-web-platform#2.0.245";

	/**
	 * simply list all parts of all solutions of the tree of the terminal
	 */
	//@Test
	public void list() {
		List<CompiledPartIdentification> parts = listParts(TEST_TERMINAL);
		for (CompiledPartIdentification cpi : parts)  {
			System.out.println(cpi.asString());
		}
	}
	
	/**
	 * download all parts of all solutions of the tree of the terminal, while filtering out '.asc', '.lastUpdated'
	 */
	//@Test
	public void download() {
		Predicate<PartIdentification> filter = new Predicate<PartIdentification>() {
			@Override
			public boolean test(PartIdentification t) {
				if (
						t.getType().endsWith( "asc") ||
						t.getType().endsWith("lastUpdated") 
					) {
					return false;
				}
				
				return true;
			}			
		};
		Pair<List<CompiledPartIdentification>, List<CompiledPartIdentification>> parts = downloadParts(TEST_TERMINAL, filter);
		System.out.println("Successful downloads : " + parts.first.size());
		for (CompiledPartIdentification cpi : parts.first)  {
			System.out.println(cpi.asString());
		}
		System.out.println("unuccessful downloads : " + parts.second.size());
		for (CompiledPartIdentification cpi : parts.second)  {
			System.out.println(cpi.asString());
		}
	}

}
