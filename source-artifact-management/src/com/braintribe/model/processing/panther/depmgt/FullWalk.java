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
package com.braintribe.model.processing.panther.depmgt;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.panther.SourceArtifact;

public class FullWalk {
	private static Logger logger = Logger.getLogger(FullWalk.class);
	private ArtifactPomReader pomReader;
	private DependencyResolver dependencyResolver;
	private Set<SourceArtifact> sourceArtifacts;
	private String walkScopeId = UUID.randomUUID().toString();
	private LinkedHashMap<String, Solution> orderedFullSolutions = new LinkedHashMap<>();
	private boolean lenient = false;
	private Set<Dependency> unresolvedDependencies = new HashSet<>();
	private boolean reduceToSourceArtifacts;
	private Predicate<Dependency> dependencyFilter = d -> true;
	
	public FullWalk(ArtifactPomReader pomReader, DependencyResolver dependencyResolver, Set<SourceArtifact> sourceArtifacts) {
		super();
		this.pomReader = pomReader;
		this.dependencyResolver = dependencyResolver;
		this.sourceArtifacts = sourceArtifacts;
	}
	
	public void setLenient(boolean lenient) {
		this.lenient = lenient;
	}
	
	public void setReduceToSourceArtifacts(boolean reduceToSourceArtifacts) {
		this.reduceToSourceArtifacts = reduceToSourceArtifacts;
	}
	
	public void setDependencyFilter(Predicate<Dependency> dependencyFilter) {
		this.dependencyFilter = dependencyFilter;
	}
	
	public Set<Dependency> getUnresolvedDependencies() {
		return unresolvedDependencies;
	}

	public List<Solution> walk() {
		return walk(reduceToSourceArtifacts, dependencyFilter);
	}
	
	public List<Solution> walk(boolean reduceToSourceArtifacts) {
		return walk(reduceToSourceArtifacts, d -> true);
	}
	
	public List<Solution> walk(boolean reduceToSourceArtifacts, Predicate<Dependency> dependencyFilter) {
		List<Solution> entrySolutions = resolveEntryDependencies();
		Deque<String> dependencyStack = new LinkedList<>();
		walk(entrySolutions, dependencyFilter, dependencyStack);
		
		if (reduceToSourceArtifacts) {
			Map<String, Solution> reducedSolutions = new LinkedHashMap<>(orderedFullSolutions);
			Set<String> names = sourceArtifacts.stream().map(a -> a.getGroupId() + ":" + a.getArtifactId() + "#" + a.getVersion()).collect(Collectors.toSet());
			reducedSolutions.keySet().retainAll(names);
			return new ArrayList<>(reducedSolutions.values());
		}
		else
			return new ArrayList<>(orderedFullSolutions.values());
	}
	
	private void walk(List<Solution> solutions, Predicate<Dependency> dependencyFilter, Deque<String> dependencyStack) {
		for (Solution solution: solutions) {
			String solutionName = NameParser.buildName(solution);
			if (dependencyStack.contains(solutionName)) {
				logger.debug("Detected a loop in "+dependencyStack);
				continue;
			}
			dependencyStack.push(solutionName);
			try {
				if (orderedFullSolutions.containsKey(solutionName))
					continue;

				logger.debug("traversing " + solutionName);

				Solution resolvedSolution = pomReader.read(walkScopeId, solution);
				List<Dependency> dependencies = extractDependencies(resolvedSolution, dependencyFilter);
				List<Solution> resolvedDependencies = resolveDependencies(dependencies);

				walk(resolvedDependencies, dependencyFilter, dependencyStack);

				orderedFullSolutions.put(solutionName, resolvedSolution);
			} catch(SolutionException se) {
				throw se;
			} catch(Exception e) {
				StringBuilder sb = new StringBuilder();
				Iterator<String> it = dependencyStack.descendingIterator();
				int indent = 0;
				while (it.hasNext()) {
					for (int i=0; i<indent; ++i) {
						sb.append(' ');
					}
					sb.append(it.next());
					sb.append('\n');
					indent++;
				}
				throw new SolutionException("Error while processin solution "+solutionName+". Dependency path: "+sb.toString(), e);
			} finally {
				dependencyStack.pop();
			}
		}
	}
	
	private List<Dependency> extractDependencies(Solution solution, Predicate<Dependency> dependencyFilter) throws Exception {
		List<Dependency> dependencies = new ArrayList<>();
		
		Solution parent = solution.getResolvedParent();
		
		if (parent != null) {
			Dependency parentDependency = Dependency.T.create();
			parentDependency.setGroupId(parent.getGroupId());
			parentDependency.setArtifactId(parent.getArtifactId());
			parentDependency.setVersionRange(VersionRangeProcessor.createfromVersion(parent.getVersion()));
			dependencies.add(parentDependency);
		}
		
		for (Solution importedSolution: solution.getImported()) {
			Dependency importDependency = Dependency.T.create();
			importDependency.setGroupId(importedSolution.getGroupId());
			importDependency.setArtifactId(importedSolution.getArtifactId());
			importDependency.setVersionRange(VersionRangeProcessor.createfromVersion(importedSolution.getVersion()));
			dependencies.add(importDependency);
		}
		
		for (Dependency dependency: solution.getDependencies()) {
			Dependency normalDependency = Dependency.T.create();
			normalDependency.setGroupId(dependency.getGroupId());
			normalDependency.setArtifactId(dependency.getArtifactId());
			//normalDependency.setVersionRange(VersionRangeProcessor.createfromVersion(extractLatest(dependency.getVersionRange())));
			normalDependency.setVersionRange(dependency.getVersionRange());
			if (dependencyFilter == null || dependencyFilter.test(dependency)) {
				dependencies.add(normalDependency);
			}
		}

		return dependencies;
	}
	
	private List<Solution> resolveEntryDependencies() {
		List<Solution> solutions = new ArrayList<>();
		
		for (SourceArtifact sourceArtifact: sourceArtifacts) {
			Dependency dependency = createDependency(sourceArtifact);
			Set<Solution> curSolutions = dependencyResolver.resolveDependency(walkScopeId, dependency);
			solutions.add(curSolutions.iterator().next());
		}
		
		return solutions;
	}
	
	private List<Solution> resolveDependencies(List<Dependency> dependencies) {
		List<Solution> solutions = new ArrayList<>();
		
		for (Dependency dependency: dependencies) {
			Set<Solution> curSolutions;
			curSolutions = dependencyResolver.resolveDependency(walkScopeId, dependency);
			
			if (curSolutions.isEmpty()) {
				if (lenient) {
					unresolvedDependencies.add(dependency);
					continue;
				}
				else {
					throw new IllegalStateException("Could not find a solution for dependency "+NameParser.buildName(dependency));
				}
			}
			
			// determine highest version of solutions
			Solution highestSolution = getHighest(curSolutions);
		
			solutions.add(highestSolution);
		}
		
		return solutions;
	}
	
	private static Comparator<Version> versionComparator = VersionProcessor.comparator.reversed();
	
	private Solution getHighest(Set<Solution> curSolutions) {
		return curSolutions.stream().sorted((s1, s2) -> versionComparator.compare(s1.getVersion(), s2.getVersion())).findFirst().orElse(null);
	}

	private Dependency createDependency(SourceArtifact sourceArtifact) {
		Dependency dependency = Dependency.T.create();
		dependency.setGroupId(sourceArtifact.getGroupId());
		dependency.setArtifactId(sourceArtifact.getArtifactId());
		dependency.setVersionRange(VersionRangeProcessor.createFromString(sourceArtifact.getVersion()));
		
		return dependency;
	}
	
}
