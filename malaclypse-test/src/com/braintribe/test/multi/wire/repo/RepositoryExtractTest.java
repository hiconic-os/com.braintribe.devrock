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
