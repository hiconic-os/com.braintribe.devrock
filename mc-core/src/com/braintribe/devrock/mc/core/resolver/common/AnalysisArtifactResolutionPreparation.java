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
package com.braintribe.devrock.mc.core.resolver.common;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.braintribe.devrock.model.mc.reason.IncompleteArtifactResolution;
import com.braintribe.devrock.model.mc.reason.IncompleteResolution;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.analysis.AnalysisTerminal;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.consumable.Artifact;
import com.braintribe.model.artifact.consumable.ArtifactResolution;
import com.braintribe.model.artifact.essential.ArtifactIdentification;

/*
 * post process the result of the parallel transitive resolvings and collate them  
 */
public class AnalysisArtifactResolutionPreparation {
	
	private int dependencyOrder = 0;
	private int visitOrder = 0;
	private final AnalysisArtifactResolution resolution;
	private final Set<AnalysisArtifact> visited = new HashSet<>();
	private final Comparator<? super AnalysisArtifact> solutionSortingComparator;
	private final Predicate<AnalysisArtifact> solutionFilter;
	
	public AnalysisArtifactResolutionPreparation(AnalysisArtifactResolution resolution) {
		this(resolution, null);
	}
	
	public AnalysisArtifactResolutionPreparation(AnalysisArtifactResolution resolution, Comparator<? super AnalysisArtifact> solutionSortingComparator) {
		this(resolution, solutionSortingComparator, s -> true);
	}
	
	public AnalysisArtifactResolutionPreparation(AnalysisArtifactResolution resolution, Comparator<? super AnalysisArtifact> solutionSortingComparator, Predicate<AnalysisArtifact> solutionFilter) {
		this.resolution = resolution;
		this.solutionSortingComparator = solutionSortingComparator;
		this.solutionFilter = solutionFilter;
	}
	
	public AnalysisArtifactResolution process() {
		
		resolution.getSolutions().clear();
		resolution.getUnresolvedDependencies().clear();
		resolution.getIncompleteArtifacts().clear();
		resolution.setFailure(null);
		
		collectSolutionsAndOmittUnprocessedDependencies();
		
		if (solutionSortingComparator != null)
			resolution.getSolutions().sort(solutionSortingComparator);
		
		return resolution;
	}

	public static void addFailures(AnalysisArtifactResolution resolution, Collection<Reason> failures) {
		Reason umbrellaReason = acquireCollatorReason(resolution);
		umbrellaReason.getReasons().addAll(failures);
	}
	
	public static void addFailures(AnalysisArtifactResolution resolution, Stream<Reason> failures) {
		Reason umbrellaReason = acquireCollatorReason(resolution);
		failures.forEach(umbrellaReason.getReasons()::add);
	}
	
	public static void addFailure(AnalysisArtifactResolution resolution, Reason failure) {
		
		Reason umbrellaReason = acquireCollatorReason(resolution);
		umbrellaReason.getReasons().add(failure);
	}

	public static Reason acquireCollatorReason(AnalysisArtifactResolution resolution) {
		Reason umbrellaReason = resolution.getFailure();
		
		if (umbrellaReason == null) {
			umbrellaReason = incompleteResolution(resolution.getTerminals());
			resolution.setFailure(umbrellaReason);
		}
		return umbrellaReason;
	}

	public static Reason acquireCollatorReason(ArtifactResolution resolution) {
		Reason umbrellaReason = resolution.getFailure();
		
		if (umbrellaReason == null) {
			umbrellaReason = incompleteResolution(resolution.getTerminals());
			resolution.setFailure(umbrellaReason);
		}
		return umbrellaReason;
	}

	private static IncompleteResolution incompleteResolution(List<? extends ArtifactIdentification> terminals) {
		return TemplateReasons.build(IncompleteResolution.T).enrich(r -> r.getTerminals().addAll(terminals)).toReason();
	}
	
	public static Reason acquireCollatorReason(Artifact artifact) {
		Reason umbrellaReason = artifact.getFailure();
		
		if (umbrellaReason == null) {
			umbrellaReason = TemplateReasons.build(IncompleteArtifactResolution.T) //
					.enrich(r -> r.setArtifact(CompiledArtifactIdentification.from(artifact))).toReason();
			
			artifact.setFailure(umbrellaReason);
		}
		
		return umbrellaReason;
	}
	
	private void collectSolutionsAndOmittUnprocessedDependencies() {
		for (AnalysisTerminal terminal: resolution.getTerminals()) {
			if (terminal instanceof AnalysisArtifact) {
				AnalysisArtifact artifact = (AnalysisArtifact)terminal;
				collectSolutionsAndOmittUnprocessedDependencies(artifact, true);
			}
			else if (terminal instanceof AnalysisDependency) {
				AnalysisDependency dependency = (AnalysisDependency)terminal;
				AnalysisArtifact artifact = dependency.getSolution();
				if (artifact != null)
					collectSolutionsAndOmittUnprocessedDependencies(artifact, false);
			}
		}
	}

	private void collectSolutionsAndOmittUnprocessedDependencies(AnalysisArtifact solution, boolean terminalArtifact) {		
		
		if (!visited.add( solution))
			return;
		
		solution.setVisitOrder(visitOrder++);
		
		if (solution.hasFailed()) {
			resolution.getIncompleteArtifacts().add(solution);
			addFailure(resolution, solution.getFailure());
		}
		
		Iterator<AnalysisDependency> it = solution.getDependencies().iterator();
		
		while (it.hasNext()) {
			AnalysisDependency dependency = it.next();
			
			AnalysisArtifact dependencySolution = dependency.getSolution();
			
			if (dependencySolution != null) {
				collectSolutionsAndOmittUnprocessedDependencies(dependencySolution, false);
			}
			else {
				Reason failure = dependency.getFailure();
				if (failure != null) {
					resolution.getUnresolvedDependencies().add(dependency);
				}
			}
		}
		
		solution.setDependencyOrder(dependencyOrder++);
		
		if (!terminalArtifact && solutionFilter.test(solution))
			resolution.getSolutions().add(solution);
	}
}
