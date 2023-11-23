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
package com.braintribe.devrock.mc.core.resolver.rulefilter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import com.braintribe.devrock.mc.api.transitive.DependencyPathElement;
import com.braintribe.model.artifact.analysis.AnalysisDependency;


/**
 * basic interface for both {@link BasicTagRuleFilter} and {@link BasicTypeRuleFilter}
 * @author pit
 *
 */
public interface TypeRuleFilter extends Predicate<AnalysisDependency> {
	/**
	 * filter a {@link List} of {@link AnalysisDependency} according their tag values
	 * @param dependencies - a {@link List} of {@link AnalysisDependency}
	 * @return - a new {@link ArrayList} with the remaining {@link AnalysisDependency}
	 */
	default List<AnalysisDependency> filter( List<AnalysisDependency> dependencies) {	
		List<AnalysisDependency> filteredList = new ArrayList<>( dependencies); 
		Iterator<AnalysisDependency> iterator = filteredList.iterator();
		while (iterator.hasNext()) {
			AnalysisDependency dependency = iterator.next();
			if (!test( dependency)) {
				iterator.remove();
			}
		}
		return filteredList;
	}
	
}
