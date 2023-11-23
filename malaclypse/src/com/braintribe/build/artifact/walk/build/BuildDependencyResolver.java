// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.build;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.braintribe.build.artifact.api.BoundaryHit;
import com.braintribe.build.artifact.api.BuildRange;
import com.braintribe.build.artifact.api.BuildRangeDependencyResolver;
import com.braintribe.build.artifact.api.BuildRangeDependencySolution;
import com.braintribe.build.artifact.api.RangedArtifact;
import com.braintribe.build.artifact.api.RangedArtifacts;
import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.retrieval.multi.enriching.MultiRepositorySolutionEnricher;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.utils.collection.api.MultiMap;
import com.braintribe.utils.collection.impl.HashMultiMap;

public class BuildDependencyResolver implements BuildRangeDependencyResolver {
	private static Logger logger = Logger.getLogger(ContextualizedBuildDependencyResolver.class);
	
	private ArtifactPomReader pomReader;
	private DependencyResolver dependencyResolver;
	private Predicate<Object> passthrough = o -> true;
	private Predicate<? super RangedArtifact> artifactFilter = passthrough;
	private Predicate<? super Dependency> dependencyFilter = passthrough;
	private Predicate<? super Solution> solutionFilter = passthrough;
	private BiPredicate<? super Solution, ? super Dependency> solutionDependencyFilter = (s,d) -> true;
	private boolean lenient = false;
	private static Comparator<Version> versionComparator = VersionProcessor.comparator.reversed();
	private MultiRepositorySolutionEnricher enricher;
	private BiConsumer<Solution, Dependency> solutionDependencyVisitor = (s,d) -> {  };
	private boolean walkParentStructure = true;
	private boolean filterSolutionBeforeVisit = false;
	
	@Configurable
	public void setFilterSolutionBeforeVisit(boolean filterSolutionBeforeVisit) {
		this.filterSolutionBeforeVisit = filterSolutionBeforeVisit;
	}
	
	@Configurable
	public void setWalkParentStructure(boolean processParentStructure) {
		this.walkParentStructure = processParentStructure;
	}
	
	@Configurable
	public void setEnricher(MultiRepositorySolutionEnricher enricher) {
		this.enricher = enricher;
	}
	
	@Configurable
	public void setLenient(boolean lenient) {
		this.lenient = lenient;
	}
	
	@Configurable
	public void setSolutionDependencyFilter(BiPredicate<? super Solution, ? super Dependency> solutionDependencyFilter) {
		this.solutionDependencyFilter = solutionDependencyFilter;
	}
	
	@Configurable
	public void setDependencyFilter(Predicate<? super Dependency> dependencyFilter) {
		this.dependencyFilter = dependencyFilter;
	}
	
	@Configurable
	public void setSolutionFilter(Predicate<? super Solution> solutionFilter) {
		this.solutionFilter = solutionFilter;
	}
	
	@Configurable
	public void setSolutionDependencyVisitor(BiConsumer<Solution, Dependency> solutionDependencyVisitor) {
		this.solutionDependencyVisitor = solutionDependencyVisitor;
	}
	
	@Configurable
	public void setArtifactFilter(Predicate<? super RangedArtifact> artifactFilter) {
		this.artifactFilter = artifactFilter;
	}
	
	@Required
	public void setPomReader(ArtifactPomReader pomReader) {
		this.pomReader = pomReader;
	}
	
	@Required
	public void setDependencyResolver(DependencyResolver dependencyResolver) {
		this.dependencyResolver = dependencyResolver;
	}
	
	@Override
	public Set<Solution> resolve(Iterable<Dependency> dependencies) {		
		return new ContextualizedBuildDependencyResolver().resolve(dependencies);
	}
	
	private ContextualizedBuildDependencyResolver createResolverFor(BuildRange config) {
		Function<RangedArtifact, BoundaryHit> lowerBoundary = config.getLowerBound();
		Function<RangedArtifact, BoundaryHit> upperBoundary = config.getUpperBound();
		
		if (lowerBoundary != null || upperBoundary != null) {
			return new ContextualizedBuildDependencyResolver(lowerBoundary, upperBoundary);
		}
		else {
			return new ContextualizedBuildDependencyResolver();
		}		
	}
	
	@Override
	public BuildRangeDependencySolution resolve(BuildRange config) {
		Iterable<Dependency> dependencies = config.getEntryPoints();
		
		ContextualizedBuildDependencyResolver resolver = createResolverFor(config);
		
		Set<Solution> solutions = resolver.resolve(dependencies);
		
		return new BuildRangeDependencySolution() {
			private MultiMap<Solution, Solution> dependencyRelations;
			
			@Override
			public Set<Solution> getSolutions() {
				return solutions;
			}
			
			@Override
			public MultiMap<Solution, Solution> getSolutionDependencyRelations() {
				if (dependencyRelations == null) {
					dependencyRelations = new HashMultiMap<>();
					
					for (Map.Entry<ResultArtifact, ResultArtifact> entry: resolver.getDependencyTree().entrySet()) {
						dependencyRelations.put(entry.getKey().solution, entry.getValue().solution);
					}
				}
				return dependencyRelations;
			}
		};
	}
	
	/**
	 * Calculates the transitive dependencies in respect to the buildRange. Solutions are removed from the result when being outside of the buildRange.
	 * @return
	 */
	@Override
	public Set<Solution> resolve(Iterable<Dependency> dependencies, Function<RangedArtifact, BoundaryHit> lowerBoundary, Function<RangedArtifact, BoundaryHit> upperBoundary) {
		return new ContextualizedBuildDependencyResolver(lowerBoundary, upperBoundary).resolve(dependencies);
	}

	private static class ResultArtifact implements RangedArtifact {
		private Solution solution;
		
		public ResultArtifact(Solution solution) {
			super();
			this.solution = solution;
		}

		@Override
		public String getGroupId() {
			return solution.getGroupId();
		}

		@Override
		public String getArtifactId() {
			return solution.getArtifactId();
		}

		@Override
		public VersionRange getVersionRange() {
			return VersionRangeProcessor.createfromVersion(solution.getVersion());
		}
		
		@Override
		public int hashCode() {
			String artifactId = solution.getArtifactId();
			String groupId = solution.getGroupId();
			String version = VersionProcessor.toString(solution.getVersion());
			final int prime = 31;
			int result = 1;
			result = prime * result + ((artifactId == null) ? 0 : artifactId.hashCode());
			result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
			result = prime * result + ((version == null) ? 0 : version.hashCode());
			
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			return ArtifactProcessor.artifactEquals(solution, ((ResultArtifact)obj).solution);
		}
	}
	
	private class ContextualizedBuildDependencyResolver implements com.braintribe.build.artifact.api.DependencyResolver {
		
		private String walkScopeId = UUID.randomUUID().toString();
		private List<Solution> orderedFullSolutions = new ArrayList<>();
		private MultiMap<ResultArtifact, ResultArtifact> dependencyTree = new HashMultiMap<>();
		private Set<Dependency> unresolvedDependencies = new HashSet<>();
		private Map<String, Solution> visitedDependencies = new HashMap<>();
		private Set<String> visitedSolutions = new HashSet<>();
		private Set<String> processingSolutions = new HashSet<>();
		private Deque<String> dependencyStack = new ArrayDeque<>();
		private Function<RangedArtifact, BoundaryHit> lowerBound;
		private Function<RangedArtifact, BoundaryHit> upperBound;
		private Set<ResultArtifact> weedingEntryPoints;
		private boolean buildRanged = false;
		private int index;
		
		
		public ContextualizedBuildDependencyResolver() {
		}
		
		public ContextualizedBuildDependencyResolver(Function<RangedArtifact, BoundaryHit> lowerBound, Function<RangedArtifact, BoundaryHit> upperBound) {
			this.lowerBound = lowerBound;
			this.upperBound = upperBound;
			this.buildRanged = true;
			this.weedingEntryPoints = new HashSet<>();
		}
		
		private LinkedHashSet<Solution> weed() {
			
			Set<ResultArtifact> harvest = new HashSet<>();
			Set<ResultArtifact> visited = new HashSet<>();
			
			for (ResultArtifact solution: weedingEntryPoints) {
				harvest(solution, harvest, visited);
			}
			
			LinkedHashSet<Solution> weededSolutions = new LinkedHashSet<>();
			
			for (Solution solution: orderedFullSolutions) {
				if (harvest.contains(new ResultArtifact(solution))) {
					weededSolutions.add(solution);
				}
			}
			
			return weededSolutions;
		}
		
		private boolean harvest(ResultArtifact solution, Set<ResultArtifact> harvest, Set<ResultArtifact> visited) {
			if (!visited.add(solution))
				return harvest.contains(solution);
			
			BoundaryHit upperHit = upperBound.apply(solution);
			BoundaryHit lowerHit = lowerBound.apply(solution);
			
			switch (upperHit) {
				case closed:
					harvest.add(solution);
					//$FALL-THROUGH$
				case open:
					break;
					
				default:
					break;
			}
			
			switch (lowerHit) {
				case closed:
					harvest.add(solution);
					//$FALL-THROUGH$
				case open:
					return true;
					
				default:
					break;
			}
			
		
			Collection<ResultArtifact> dependencies = dependencyTree.getAll(solution);
			
			if (dependencies.isEmpty()) {
				if (lowerBound.apply(RangedArtifacts.boundaryFloor()) == BoundaryHit.open) {
					harvest.add(solution);
					return true;
				}
				return false;
			}

			boolean connectedToLowerBound = false;
			
			for (ResultArtifact dependency: dependencies) {
				if (harvest(dependency, harvest, visited) && upperHit == BoundaryHit.none) {
					harvest.add(solution);
					connectedToLowerBound = true;
				}
			}
			
			return connectedToLowerBound;
			
		}


		public MultiMap<ResultArtifact, ResultArtifact> getDependencyTree() {
			return dependencyTree;
		}
		
		@Override
		public Set<Solution> resolve(Iterable<Dependency> dependencies) {
			index = 0;
			walk(dependencies);

			Set<Solution> filteredSolutions = buildRanged? weed(): new LinkedHashSet<>(orderedFullSolutions);
			
			if (enricher != null)
				enricher.enrich(walkScopeId, filteredSolutions);
			
			return filteredSolutions;
		}
		
		private void walk(Iterable<Dependency> dependencies) {
			for (Dependency dependency: dependencies) {
				walk(dependency, null);
			}
		}
		
		private void walk(Dependency dependency, Solution requestor) {
			Solution resolvedSolution = visitedDependencies.computeIfAbsent(NameParser.buildName(dependency), k -> {
				return resolveDependencies(dependency);
			});
			
			if (resolvedSolution != null) {
				resolvedSolution.getRequestors().add(dependency);
				walk(resolvedSolution, requestor);
			}
		}
		
		private void walk(Solution solution, Solution depender) {
			ResultArtifact solutionArtifact = new ResultArtifact(solution);
			if (depender != null)
				dependencyTree.put(new ResultArtifact(depender), solutionArtifact);

			walk(solution);
		}

		private void walk(Solution solution) {
			String solutionName = NameParser.buildName(solution);
			
			dependencyStack.push(solutionName);
			
			try {
				
				if (!processingSolutions.add(solutionName)) {
					throw new IllegalStateException("Detected a loop in "+dependencyStack);
				}

				if (filterSolutionBeforeVisit && !filterSolution(solution))
					return;
				
				if (!visitedSolutions.add(solutionName)) {
					return;
				}
				
				if (!filterSolutionBeforeVisit && !filterSolution(solution))
					return;
				
				logger.debug("traversing " + solutionName);
				
				Solution resolvedSolution = pomReader.read(walkScopeId, solution);
				
				if (walkParentStructure) {
					Stream.concat(resolvedSolution.getImported().stream(), Stream.of(resolvedSolution.getResolvedParent()))
					.filter(s -> s != null)
					.forEach(s -> walk(s, solution));
				}
				
				// wire dependency and resolved solution  
				resolvedSolution.getDependencies().stream()
				.forEach( d -> {
								d.getSolutions().add(resolvedSolution);								
						}
				);
				
				resolvedSolution.getDependencies().stream()
					.filter(d -> solutionDependencyFilter.test(resolvedSolution, d))
					.filter(this::filterDependency)
					.peek(d -> solutionDependencyVisitor.accept(resolvedSolution, d))
					.forEach(d -> walk(d, solution));
					
				
				orderedFullSolutions.add(resolvedSolution);
				
				if (buildRanged) {
					ResultArtifact solutionArtifact = new ResultArtifact(solution);
					
					if (upperBound.apply(solutionArtifact) != BoundaryHit.none) {
						weedingEntryPoints.add(solutionArtifact);
					}
				}
				
				resolvedSolution.setOrder( index++);
			} catch(SolutionException se) {
				throw se;
			} catch(Exception e) {
				StringBuilder sb = buildStackInfo(dependencyStack);
				throw new SolutionException("Error while processing solution "+solutionName + "(" + e.getMessage() + "). Dependency path: "+ sb, e);
			} finally {
				dependencyStack.pop();
				processingSolutions.remove(solutionName);
			}
		}
		
		private boolean filterSolution(Solution s) {
			return solutionFilter.test(s) && artifactFilter.test(RangedArtifacts.from(s));
		}
		
		private boolean filterDependency(Dependency d) {
			boolean result = false;
			if (dependencyFilter.test(d) && artifactFilter.test(RangedArtifacts.from(d))) {
				if (d.getArtifactId().endsWith("-view") && d.getVersionRange().getOriginalVersionRange().matches("\\d+\\.\\d+\\.\\d+")) {
					// we allow view assets to depend on other views (even within the same group) using a concrete version.
					// in that case we just filter out the dependency, i.e. exclude it from the transitive build.
					// this is correct, since it's anyway a concrete and already published version, i.e. no need to rebuild.
					System.out.println("Filtering out dependency on view " + d.getGroupId() + ":" + d.getArtifactId() + "#" + d.getVersionRange().getOriginalVersionRange() + " since it's a concrete version.");
				} else {
					result = true;
				}
			}
			return result;
		}

		private StringBuilder buildStackInfo(Deque<String> dependencyStack) {
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
			return sb;
		}
		
		private Solution resolveDependencies(Dependency dependency) {
			Set<Solution> solutions = dependencyResolver.resolveDependency(walkScopeId, dependency);
			
			if (solutions.isEmpty()) {
				unresolvedDependencies.add(dependency);
				String msg = "Could not find a solution for dependency "+NameParser.buildName(dependency);
				if (lenient) {
					logger.warn(msg);
					return null;
				}
				else {
					throw new IllegalStateException(msg);
				}
			}
			
			// determine highest version of solutions
			Solution highestSolution = getHighest(solutions);

			return highestSolution;
		}
		
		private Solution getHighest(Set<Solution> curSolutions) {
			return curSolutions.stream().sorted((s1, s2) -> versionComparator.compare(s1.getVersion(), s2.getVersion())).findFirst().orElse(null);
		}

	}
}
