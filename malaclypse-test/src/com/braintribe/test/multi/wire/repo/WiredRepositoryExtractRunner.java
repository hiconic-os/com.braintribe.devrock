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
