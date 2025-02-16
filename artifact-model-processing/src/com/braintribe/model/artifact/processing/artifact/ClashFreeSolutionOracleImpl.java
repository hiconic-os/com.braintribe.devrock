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
package com.braintribe.model.artifact.processing.artifact;

import static com.braintribe.utils.lcd.CollectionTools2.acquireList;
import static com.braintribe.utils.lcd.CollectionTools2.newLinkedSet;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static java.util.Collections.emptyList;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.api.ClashFreeSolutionOracle;
import com.braintribe.model.generic.reflection.GenericModelException;

/**
 * Simple {@link ClashFreeSolutionOracle} implementation.
 * 
 * @author peter.gazdik
 */
public class ClashFreeSolutionOracleImpl implements ClashFreeSolutionOracle {

	private final Map<String, Solution> solutionsByName = newMap();
	private final Map<String, List<Solution>> solutionsToDependers = newMap();

	public ClashFreeSolutionOracleImpl(Collection<Solution> solutions) {
		this(solutions, false);
	}

	public ClashFreeSolutionOracleImpl(Collection<Solution> solutions, boolean parentIsDependency) {
		indexByName(solutions);
		indexDependencies(solutions);
		indexParentAsDependencyIfRelevant(solutions, parentIsDependency);
	}

	// Index build

	private void indexByName(Collection<Solution> solutions) {
		for (Solution solution : solutions)
			solutionsByName.put(artifactName(solution), solution);
	}

	private void indexDependencies(Collection<Solution> solutions) {
		for (Solution solution : solutions)
			for (Dependency dependency : solution.getDependencies())
				markDependency(solution, dependency);
	}

	private void indexParentAsDependencyIfRelevant(Collection<Solution> solutions, boolean parentIsDependency) {
		if (parentIsDependency)
			for (Solution solution : solutions)
				markDependency(solution, solution.getParent());
	}

	private void markDependency(Solution depender, Dependency dependency) {
		if (dependency == null)
			return;

		String dependencyName = artifactName(dependency);
		String dependerName = artifactName(depender);

		// just to be sure in case the input is corrupt
		if (dependerName.equals(dependencyName))
			return;

		Solution dependencySolution = findSolution(dependencyName);
		if (dependencySolution != null)
			acquireList(solutionsToDependers, dependencyName).add(depender);
	}

	// Index access

	@Override
	public List<Solution> getDirectDependers(Solution solution) {
		// There doesn't have to be a depender for given solution
		return solutionsToDependers.computeIfAbsent(artifactName(solution), n -> emptyList());
	}

	@Override
	public Solution getSolution(String versionlessName) {
		return solutionsByName.computeIfAbsent(versionlessName, n -> {
			throw new GenericModelException("No solution found with name: " + versionlessName);
		});
	}

	@Override
	public Solution findSolution(String versionlessName) {
		return solutionsByName.get(versionlessName);
	}

	private String artifactName(Identification artifact) {
		return artifactName(artifact.getGroupId(), artifact.getArtifactId());
	}

	private String artifactName(String groupId, String artifactId) {
		return groupId + ":" + artifactId;
	}

	// Dependency resolutions

	@Override
	public Set<Solution> resolveDependers(Solution solution) {
		return resolveDependers(Collections.singletonList(solution));
	}

	@Override
	public Set<Solution> resolveDependers(Collection<Solution> solutions) {
		return new DependencyResolution(solutions).resolveDependers();
	}

	private class DependencyResolution {
		private final Set<Solution> visited = newLinkedSet();
		private final Deque<Solution> toVisit = new ArrayDeque<>();

		public DependencyResolution(Collection<Solution> solutions) {
			toVisit.addAll(solutions);
		}

		private Set<Solution> resolveDependers() {
			while (!toVisit.isEmpty())
				visit(toVisit.pop());
			return visited;
		}

		private void visit(Solution solution) {
			if (visited.add(solution))
				for (Solution depender : getDirectDependers(solution))
					rememberToVisit(depender);
		}

		private void rememberToVisit(Solution depender) {
			toVisit.push(depender);
		}

	}

}
