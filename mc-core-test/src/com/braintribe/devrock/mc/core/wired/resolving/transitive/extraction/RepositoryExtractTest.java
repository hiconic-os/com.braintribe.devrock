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
