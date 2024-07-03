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
package com.braintribe.test.multi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.walk.multi.WalkException;
import com.braintribe.build.artifact.walk.multi.Walker;
import com.braintribe.model.artifact.Solution;

public class WalkingExpert {

	
	public static Collection<Solution> walk(String walkScopeId, Solution terminal, String[] expectedNames, Walker walker, boolean sort) throws WalkException {
		return walk(walkScopeId, terminal, expectedNames, 1, 0, walker, sort);
	}
	public static Collection<Solution> walk(String walkScopeId, Solution terminal, String[] expectedNames, int repeat, int threshold, Walker walker, boolean sort) throws WalkException {
		Collection<Solution> solutions = null;
		long sum = 0;
		for (int i = 0; i < repeat; i++) {
			long before = System.nanoTime();
			solutions = walker.walk( walkScopeId, terminal);
			long after = System.nanoTime();
			if (i >= threshold) {
				sum += (after-before);
			}
		}
		
		long average = sum / (repeat-threshold);
		System.out.println("walking of [" + NameParser.buildName(terminal) + "] took [" + (average / 1E6) + "] ms averaged over [" + (repeat-threshold) + "] runs");
		
		if (expectedNames != null ) {
			if (expectedNames.length == 0) {
				Assert.assertTrue("solutions returned", solutions == null || solutions.size() == 0);
			}
			else {
				Assert.assertTrue("No solutions returned", solutions != null && solutions.size() > 0);
			}					
		}
		List<Solution> sorted = new ArrayList<Solution>( solutions);
		if (sort) {
			Collections.sort( sorted, new Comparator<Solution>() {
	
				@Override
				public int compare(Solution arg0, Solution arg1) {
					return arg0.getArtifactId().compareTo( arg1.getArtifactId());					
				}
				
			});			
		}
		if (expectedNames == null || expectedNames.length == 0)
			return solutions;
		if (expectedNames.length > 0) {
			boolean result = WalkResultValidationExpert.listDiscrepancies(sorted, expectedNames);			
			if (!result) {
				Assert.fail("Test result is not as expected");
			}
		}
		return solutions;
	}
}
