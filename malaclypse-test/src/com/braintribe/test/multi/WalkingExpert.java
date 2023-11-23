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
