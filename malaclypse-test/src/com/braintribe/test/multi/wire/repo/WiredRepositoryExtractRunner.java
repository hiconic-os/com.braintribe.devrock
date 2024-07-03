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

import java.util.List;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.build.artifacts.mc.wire.repositoryExtract.RepositoryExtractWireModule;
import com.braintribe.build.artifacts.mc.wire.repositoryExtract.contract.RepositoryExtractContract;
import com.braintribe.build.artifacts.mc.wire.repositoryExtract.expert.RepositoryExtractRunner;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

@Category(KnownIssue.class)
public class WiredRepositoryExtractRunner {

	
	private Map<String, List<String>>  test(String terminal, String ... exclusions) {
		RepositoryExtractWireModule extract = new RepositoryExtractWireModule( terminal, exclusions);
		
		WireContext<RepositoryExtractContract> wireContext = Wire.context( extract);
		
		RepositoryExtractRunner runner = wireContext.contract().extractor();
		
		Map<String, List<String>> extracted = runner.runExtraction();
		
		return extracted;		
	}
	
	private void output( Map<String, List<String>> result) {
		
		for (Entry<String, List<String>> entry : result.entrySet()) {
			System.out.println( entry.getKey());
			for (String str : entry.getValue()) {
				System.out.println( "\t" + str);
			}
		}
	}

	@Test
	public void adx_standard_setup() {
		Map<String, List<String>> result = test( "tribefire.adx.phoenix:adx-standard-setup#2.0", "com.sun.*");
		output(result);
	}
	
}
