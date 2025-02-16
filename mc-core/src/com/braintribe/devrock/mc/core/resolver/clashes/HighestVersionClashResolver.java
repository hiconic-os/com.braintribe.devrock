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
package com.braintribe.devrock.mc.core.resolver.clashes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.braintribe.cc.lcd.EqProxy;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.model.mc.reason.IncompleteClashResolving;
import com.braintribe.devrock.model.mc.reason.InvalidClashResolvingWinner;
import com.braintribe.devrock.model.mc.reason.UnresolvableClash;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.analysis.DependencyClash;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.version.Version;
import com.braintribe.model.version.VersionInterval;



/**
 * clash resolver that chooses the highest version to resolve clashes
 * @author pit / dirk
 *
 */
public class HighestVersionClashResolver {
	//private static final Comparator<Dependency> dependencyVersionComparator = HighestVersionClashResolver::compareVersions;
	
	private Map<EqProxy<ArtifactIdentification>, List<AnalysisDependency>> clashes;
	private Map<AnalysisDependency, List<AnalysisDependency>> clashesInvolvment = new IdentityHashMap<AnalysisDependency, List<AnalysisDependency>>();
	private Iterable<AnalysisDependency> dependencies;
	private SortedSet<DependencyClash> dependencyClashes = new TreeSet<>(ArtifactIdentification::compareTo);
	
	private Set<AnalysisArtifact> orderVisited = new HashSet<>();

	private int visitOrder = 0;
	private int dependencyOrder = 0;
			
	HighestVersionClashResolver(Iterable<AnalysisDependency> dependencies) {
		this.dependencies = dependencies;
	}
	
	public static void resolve( Iterable<AnalysisDependency> dependencies) {
		new HighestVersionClashResolver(dependencies).resolve();
	}
	
	private static int compareVersions(AnalysisDependency d1, AnalysisDependency d2) {
		Version v1 = getLowestBoundVersion(d1);
		Version v2 = getLowestBoundVersion(d2);
		
		return v1.compareTo(v2);
	}

	private static Version getLowestBoundVersion(AnalysisDependency d1) {
		List<VersionInterval> intervals = d1.getOrigin().getVersion().asVersionIntervalList();
		
		Version lowestBound = null;
		
		for (VersionInterval interval: intervals) {
			Version lowerBound = interval.lowerBound();
			
			if (lowestBound == null)
				lowestBound = lowerBound;
			else if (lowerBound.compareTo(lowestBound) < 0)
				lowestBound = lowerBound;
		}
		
		return lowestBound;
	}
	
	public Maybe<List<DependencyClash>> resolve() {
		clashes = ClashDetector.detect(dependencies);
		
		buildClashInvolvements();
		
		List<Reason> failures = resolveClashesWithHighestVersion();
		
		buildVisitAndDependencyOrder();
		
		if (failures == null || failures.isEmpty()) {
			return Maybe.complete(new ArrayList<>(dependencyClashes));
		}
		else  {
			Reason umbrellaReason = IncompleteClashResolving.T.create();
			umbrellaReason.getReasons().addAll(failures);		
			return Maybe.incomplete(new ArrayList<>(dependencyClashes), umbrellaReason);
		}
	}

	private void buildVisitAndDependencyOrder() {
		for (AnalysisDependency dependency: dependencies) {
			visitDependencyForOrdering(dependency);
		}
	}
	
	private void visitSolutionForOrdering(AnalysisArtifact analysisArtifact) {
		if (!orderVisited.add(analysisArtifact))
			return;
		
		analysisArtifact.setVisitOrder(visitOrder++);
		
		for (AnalysisDependency dependency: analysisArtifact.getDependencies()) {
			visitDependencyForOrdering(dependency);
		}
		
		analysisArtifact.setDependencyOrder(dependencyOrder++);
	}
	
	private void visitDependencyForOrdering(AnalysisDependency dependency) {
		AnalysisArtifact solution = dependency.getSolution();
		
		if (solution != null)
			visitSolutionForOrdering(solution);
	}

	private List<Reason> resolveClashesWithHighestVersion() {
		Set<AnalysisArtifact> visited = new HashSet<AnalysisArtifact>();
		List<List<AnalysisDependency>> orderedClashes = new ArrayList<List<AnalysisDependency>>();
		
		List<AnalysisDependency> clashingDependencies = clashes.values().stream().flatMap(l -> l.stream()).collect(Collectors.toList());
		
		for (AnalysisDependency clashingDependency: clashingDependencies) {
			collectClashesInClashDependerOrder(clashingDependency.getSolution(), visited, orderedClashes);
		}
		List<Reason> collectedErrors = new ArrayList<>();
		for (List<AnalysisDependency> clashes: orderedClashes) {
			
			Reason failure = resolve(clashes);
			if (failure != null) {
				collectedErrors.add( failure);
			}
		}
		return collectedErrors;
	}
	
	private void collectClashesInClashDependerOrder(AnalysisArtifact artifact, Set<AnalysisArtifact> visited, List<List<AnalysisDependency>> orderedClashes) {
		if (artifact == null)
			return;
		
		if (!visited.add(artifact))
			return;
		
		EqProxy<ArtifactIdentification> key = HashComparators.artifactIdentification.eqProxy(artifact);
		List<AnalysisDependency> clashList = clashes.get(key);
		
		if (clashList == null) {
			for (AnalysisDependency depender: artifact.getDependers()) {
				AnalysisArtifact dependerSolution = depender.getDepender();
				collectClashesInClashDependerOrder(dependerSolution, visited, orderedClashes);
			}
		}
		else {
			for (AnalysisDependency dependency: clashList) {
				AnalysisArtifact altArtifact = dependency.getSolution();
				
				if (altArtifact == null)
					continue;
				
				for (AnalysisDependency depender: altArtifact.getDependers()) {
					AnalysisArtifact dependerSolution = depender.getDepender();
					collectClashesInClashDependerOrder(dependerSolution, visited, orderedClashes);
				}
			}
			
			orderedClashes.add(clashList);
		}
	}

	private Reason resolve(List<AnalysisDependency> clashes) {
		Reason failReason = null;
		
		AnalysisDependency winner = getBestMatchForStrategy(clashes);
		
		if (winner == null)
			return null;
		
		AnalysisArtifact winnerSolution = winner.getSolution();
		if (winnerSolution == null) {			
			String msg = "winner dependency [" +  buildDependencyOutput(winner) + "] amongst [" + clashes.stream().map( d -> buildDependencyOutput(d)).collect(Collectors.joining(",")) + "] has no solution";
			System.out.println(msg);
			failReason = TemplateReasons.build(InvalidClashResolvingWinner.T)
					.assign( InvalidClashResolvingWinner::setDependency, buildDependencyOutput(winner))
					.assign( InvalidClashResolvingWinner::setChoices, clashes.stream().map( d -> buildDependencyOutput(d)).collect(Collectors.joining(",")))
					.toReason();
			return failReason;
		}

		DependencyClash dependencyClash = DependencyClash.T.create();
		dependencyClash.setSolution(winnerSolution);
		dependencyClash.setGroupId(winner.getGroupId());
		dependencyClash.setArtifactId(winner.getArtifactId());
		dependencyClash.setSelectedDependency(winner);
		
		AnalysisDependency[] copiedClashes = clashes.toArray(new AnalysisDependency[clashes.size()]);
		
		for (AnalysisDependency dependency : copiedClashes) {
			if (dependency.getVersion() == null) {
				continue;
			}
			dependencyClash.getInvolvedDependencies().add(dependency);
			if (dependency.getSolution() != winnerSolution) {
				AnalysisArtifact replacedSolution = dependency.getSolution();
				dropSolutionFromDependency(dependency);
				dependency.setSolution( winnerSolution);
				dependency.setFailure(winner.getFailure());
				winnerSolution.getDependers().add(dependency);
				
				dependencyClash.getReplacedSolutions().put(dependency, replacedSolution);
			}
		}
		
		dependencyClashes.add(dependencyClash);
		return failReason;
	}

	/**
	 * show the {@link AnalysisDependency} plus its origin
	 * @param d - the {@link AnalysisDependency}
	 * @return - a {@link String} showing the {@link AnalysisDependency} plus - if possible - it's owning {@link CompiledArtifact}
	 */
	private String buildDependencyOutput(AnalysisDependency d) {
		CompiledDependency compiledDependency = d.getOrigin();
		if (compiledDependency == null)  {
			return d.asString() + "(unknown)";
		}
		CompiledArtifact compiledArtifact = compiledDependency.getOrigin();
		if (compiledArtifact == null) {
			return d.asString() + "(unknown)";
		}		
		return d.asString() + "(" + compiledArtifact.asString() + ")";
	}

	private void buildClashInvolvements() {
		for (List<AnalysisDependency> clashList: clashes.values()) {
			for (AnalysisDependency dependency: clashList) {
				clashesInvolvment.put(dependency, clashList);
			}
		}
	}
	
	private void dropSolutionFromDependency(AnalysisDependency dependency) {
		List<AnalysisDependency> clashList = clashesInvolvment.get(dependency);
		
		if (clashList != null) {
			clashList.remove(dependency);
		}
		
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
	

	private static AnalysisDependency getBestMatchForStrategy( List<AnalysisDependency> dependencies) {
		AnalysisDependency winner = null;
		Reason clashFailure = null;
		
		for (AnalysisDependency dependency: dependencies) {
			if (dependency.getOrigin().getVersion() == null) {
				if (clashFailure == null)
					clashFailure = Reasons.build(UnresolvableClash.T).text("Could not resolve clash due to a missing version on one of the dependencies").toReason();
				
				clashFailure.getReasons().add(dependency.getFailure());
				
				continue;
			}
			/*
			 * AS WRITTEN (and works because of Dependency.compareTo
			if (winner == null || winner.compareTo(dependency) < 0)
				winner = dependency;
			*/
			
			// prob' AS INTENDED : using comparator 
			if (winner == null || compareVersions(winner, dependency) < 0)
				winner = dependency;
			
		}
		
		if (winner != null) {
			if (winner.getFailure() != null) {
				if (clashFailure == null) {
					clashFailure = Reasons.build(UnresolvableClash.T).text("Could not resolve clash due to a missing version on one of the dependencies").toReason();
				}
				clashFailure.getReasons().add(winner.getFailure());
			}
			
			winner.setFailure(clashFailure);
		}
		
		return winner;
	}
}
