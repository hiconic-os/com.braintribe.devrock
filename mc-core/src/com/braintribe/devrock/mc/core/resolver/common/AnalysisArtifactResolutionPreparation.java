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
