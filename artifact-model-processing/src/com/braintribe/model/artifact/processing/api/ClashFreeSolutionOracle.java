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
package com.braintribe.model.artifact.processing.api;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.braintribe.model.artifact.Solution;

/**
 * Utility class which provides higher level functionality on top of a collection of {@link Solution}s, assuming the
 * solutions contain no clashes.
 * 
 * Experimental, immature, don't really use it.
 * 
 * 
 * @author peter.gazdik
 */
public interface ClashFreeSolutionOracle {

	Solution getSolution(String versionlessName);

	Solution findSolution(String versionlessName);

	List<Solution> getDirectDependers(Solution solution);

	Set<Solution> resolveDependers(Solution solution);

	Set<Solution> resolveDependers(Collection<Solution> solutions);

}
