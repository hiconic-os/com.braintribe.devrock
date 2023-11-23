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
package com.braintribe.devrock.mc.core.resolver.clashes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.analysis.DependencyClash;
import com.braintribe.model.artifact.essential.ArtifactIdentification;

/**
 * clash resolver that mimics Maven's first visit clash resolving
 * @author pit / dirk
 *
 */
public class FirstVisitClashResolver {
	private Iterable<AnalysisDependency> dependencies;
	private Set<AnalysisArtifact> visited = new HashSet<>();
	private Map<EqProxy<ArtifactIdentification>, List<AnalysisDependency>> collectedDependencies = new HashMap<EqProxy<ArtifactIdentification>, List<AnalysisDependency>>();
	private int dependencyOrder = 0;
	private int visitOrder = 0;
	private Map<EqProxy<ArtifactIdentification>, DependencyClash> clashes = new HashMap<>();
			
	FirstVisitClashResolver(Iterable<AnalysisDependency> dependencies) {
		this.dependencies = dependencies;
	}
	
	public static void resolve( Iterable<AnalysisDependency> dependencies) {
		new FirstVisitClashResolver(dependencies).resolve();
	}
	
	public List<DependencyClash> resolve() {
		for (AnalysisDependency dependency: dependencies) {
			resolve(dependency);
		}
		
		return clashes.values().stream().sorted(ArtifactIdentification::compareTo).collect(Collectors.toList());
	}
	
	private void resolve(AnalysisDependency dependency) {
		AnalysisArtifact solution = dependency.getSolution();
		
		EqProxy<ArtifactIdentification> key = HashComparators.artifactIdentification.eqProxy(dependency);
		
		List<AnalysisDependency> dependencies = collectedDependencies.compute(key, (k,l) -> updateDependencies(l, dependency));
		
		AnalysisDependency selectedDependency = dependencies.get(0);
		
		AnalysisArtifact selectedSolution = selectedDependency.getSolution();
		
		if (selectedSolution != solution) {
			clashes.compute(key, (k,c) -> updateClash(c, dependencies, dependency, solution));
			
			dropSolutionFromDependency(dependency);
			
			if (selectedSolution != null) {
				dependency.setSolution(selectedSolution);
				selectedSolution.getDependers().add(dependency);
			}
			else {
				dependency.setFailure(selectedDependency.getFailure());
			}
		}
		else {
			if (solution != null)
				resolve(solution);
		}
	}
	
	private List<AnalysisDependency> updateDependencies(List<AnalysisDependency> dependencies, AnalysisDependency dependency) {
		if (dependencies == null)
			return Collections.singletonList(dependency);
		
		
		if (dependencies.size() == 1) {
			dependencies = new ArrayList<>(dependencies);
		}
			
		dependencies.add(dependency);
		
		return dependencies;
	}

	private DependencyClash updateClash(DependencyClash clash, List<AnalysisDependency> collectedDependencies, AnalysisDependency dependency, AnalysisArtifact replacedSolution) {
		boolean newClash = clash == null;
		
		if (newClash) {
			AnalysisDependency selectedDependency = collectedDependencies.get(0);
			
			AnalysisArtifact selectedSolution = selectedDependency.getSolution();
			clash = DependencyClash.T.create();
			clash.setSolution(selectedSolution);
			clash.setGroupId(selectedDependency.getGroupId());
			clash.setArtifactId(selectedDependency.getArtifactId());
			clash.setSelectedDependency(selectedDependency);
			clash.getInvolvedDependencies().addAll(collectedDependencies);
		}
		
		if (!newClash)
			clash.getInvolvedDependencies().add(dependency);
		
		clash.getReplacedSolutions().put(dependency, replacedSolution);

		return clash;
	}
	
	private void dropSolutionFromDependency(AnalysisDependency dependency) {
		AnalysisArtifact solution = dependency.getSolution();
		
		if (solution != null) {
			dependency.setSolution(null);
			Set<AnalysisDependency> dependers = solution.getDependers();
			dependers.remove(dependency);
			
			// check orphanized solution and drop it in case as well
			if (dependers.isEmpty()) {
				for (AnalysisDependency ophanizedSolutionDependency: solution.getDependencies()) {
					dropSolutionFromDependency(ophanizedSolutionDependency);
				}
			}
		}
	}

	
	private void resolve(AnalysisArtifact solution) {
		if (!visited.add(solution))
			return;
		
		solution.setVisitOrder(visitOrder++);
		
		for (AnalysisDependency curDependency: solution.getDependencies()) {
			resolve(curDependency);
		}
		
		solution.setDependencyOrder(dependencyOrder++);
	}
}
