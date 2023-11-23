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
package com.braintribe.test.multi.wire.repo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.KnownIssue;
@Category(KnownIssue.class)
public class RepositoryExtractTest {

	
	private Map<String,List<String>> runTest( String terminal, String ... exclusions) {
		RepositoryExtractRunner runner = new RepositoryExtractRunner();
		
		if (exclusions != null) {
			List<Pattern> patterns = new ArrayList<>();
			for (String exclusion : exclusions) {
				patterns.add( Pattern.compile(exclusion));
			}
			runner.setGlobalExclusions(patterns);
		}
		List<String> terminalNames = Collections.singletonList( terminal);
	
		return runner.extractArtifacts( terminalNames);
		
	}
	
	@Test
	public void test() {
		Map<String, List<String>> result = runTest( "tribefire.adx.phoenix:adx-standard-setup#2.0", "com.sun.*");//, "com.documentum.*", "com.sun.*");
		for (Entry<String, List<String>> entry : result.entrySet()) {
			System.out.println( entry.getKey());
			System.out.println( entry.getValue().stream().collect(Collectors.joining("\t\n")));
		}
	}
}
