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
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.processing.async.api.Promise;
import com.braintribe.processing.async.impl.HubPromise;
import com.braintribe.utils.collection.api.MultiMap;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.utils.collection.impl.HashMultiMap;

public class ParallelBuildDependencyResolver implements BuildRangeDependencyResolver, LifecycleAware {
	private static Logger logger = Logger.getLogger(ContextualizedBuildDependencyResolver.class);
	
	private ArtifactPomReader pomReader;
	private DependencyResolver dependencyResolver;
	private final Predicate<Object> passthrough = o -> true;
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
	private boolean respectExclusions = false;
	
	private ExecutorService executorService;
	
	@Configurable
	public void setRespectExclusions(boolean respectExclusions) {
		this.respectExclusions = respectExclusions;
	}
	
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
	public void postConstruct() {
		executorService = Executors.newFixedThreadPool(10);
	}
	
	@Override
	public void preDestroy() {
		executorService.shutdown();
	}
	
	private <T> Promise<T> submit(Callable<T> callable) {
		HubPromise<T> promise = new HubPromise<>();
		
		AttributeContext parentThreadContext = AttributeContexts.peek();
		
		executorService.execute(() -> {
			AttributeContexts.push(parentThreadContext);
			try {
				promise.accept(callable.call());
			}
			catch (Throwable e) {
				promise.onFailure(e);
			}
			finally {
				AttributeContexts.pop();
			}
		});
		
		return promise;
	}
	
	private List<Solution> getParentDependencies(ContextualizedBuildDependencyResolver resolver, List<String> present, Solution suspect) {
		List<Solution> result = new ArrayList<>();
		Set<Solution> linkedSolutions = resolver.getLinkedSolutions( suspect);
		if (linkedSolutions == null)
			return result;
		for (Solution linked : linkedSolutions) {
			String name = NameParser.buildName( linked);
			if (present.contains( name)) {
				continue;
			}
			// 
			present.add( name);
			List<Solution> list = getParentDependencies(resolver, present, linked);
			result.addAll( list);
			result.add( linked);
		}
		return result;
	}
	
	@Override
	public Set<Solution> resolve(Iterable<Dependency> dependencies) {		
		ContextualizedBuildDependencyResolver contextualizedBuildDependencyResolver = new ContextualizedBuildDependencyResolver();
		Set<Solution> result = contextualizedBuildDependencyResolver.resolve(dependencies);
		
		if (walkParentStructure) {						
			List<String> present = result.stream().map( s -> NameParser.buildName(s)).collect( Collectors.toList());
			Set<Solution> complete = new LinkedHashSet<>();
			for (Solution solution : result) {				
				List<Solution> parentDependencies = getParentDependencies(contextualizedBuildDependencyResolver, present, solution);
				complete.addAll( parentDependencies);
				complete.add( solution);
			}
			return complete;
		}
		else {							
			return result;
		}
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
	 */
	@Override
	public Set<Solution> resolve(Iterable<Dependency> dependencies, Function<RangedArtifact, BoundaryHit> lowerBoundary, Function<RangedArtifact, BoundaryHit> upperBoundary) {
		return new ContextualizedBuildDependencyResolver(lowerBoundary, upperBoundary).resolve(dependencies);
	}

	private static class ResultArtifact implements RangedArtifact {
		private final Solution solution;
		
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
		
		private final String walkScopeId = UUID.randomUUID().toString();
		private final List<Solution> orderedFullSolutions = new ArrayList<>();
		private final MultiMap<ResultArtifact, ResultArtifact> dependencyTree = new HashMultiMap<>();
		private final Map<RangedArtifact, Promise<Solution>> resolvedDependencies = new ConcurrentHashMap<>();
		private final Map<RangedArtifact, Solution> identityManagedSolutions = new ConcurrentHashMap<>();
		private final Set<Dependency> unresolvedDependencies = new HashSet<>();
		private final Set<String> visitedSolutions = new HashSet<>();
		private Function<RangedArtifact, BoundaryHit> lowerBound;
		private Function<RangedArtifact, BoundaryHit> upperBound;
		private Set<ResultArtifact> weedingEntryPoints;
		private boolean buildRanged = false;
		private final AtomicInteger runningTraversions = new AtomicInteger();
		private final Object runningMonitor = new Object();
		private List<Throwable> asyncErrors;
		private final Object asyncErrorMonitor = new Object();
		
		public ContextualizedBuildDependencyResolver() {
		}
		
		public ContextualizedBuildDependencyResolver(Function<RangedArtifact, BoundaryHit> lowerBound, Function<RangedArtifact, BoundaryHit> upperBound) {
			this.lowerBound = lowerBound;
			this.upperBound = upperBound;
			this.buildRanged = true;
			this.weedingEntryPoints = Collections.synchronizedSet(new HashSet<>());
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
		
		public Set<Solution> getLinkedSolutions( Solution solution) {
			Object obj = dependencyTree.get( new ResultArtifact(solution));
			if (obj == null)
				return null;
			if (obj instanceof Set) {
				@SuppressWarnings("unchecked")
				Set<ResultArtifact> ars = (Set<ResultArtifact>) obj;				
				return ars.stream().map( ar -> ar.solution).collect( Collectors.toSet());
			}
			return Collections.singleton( ((ResultArtifact) obj).solution) ;
		}
		
		@Override
		public Set<Solution> resolve(Iterable<Dependency> dependencies) {
			List<Dependency> topLevelDependencies = new ArrayList<>();
			
			for (Dependency dependency: dependencies) {
				Dependency topLevelDependency = Dependency.T.create();
				topLevelDependency.setGroupId(dependency.getGroupId());
				topLevelDependency.setArtifactId(dependency.getArtifactId());
				topLevelDependency.setVersionRange(dependency.getVersionRange());
				topLevelDependencies.add(topLevelDependency);
			}
			
			walk(topLevelDependencies);
			
			synchronized (runningMonitor) {
				try {
					runningMonitor.wait();
				} catch (InterruptedException e) {
					Exceptions.unchecked(e, "Unexpected interruption");
				}
			}
			
			checkAsyncErrorsAndThrowIfNecessary();
			
			new BuildOrderWalker().determineBuildOrder(topLevelDependencies);

			Set<Solution> filteredSolutions = buildRanged? weed(): new LinkedHashSet<>(orderedFullSolutions);
			
			if (enricher != null)
				enricher.enrich(walkScopeId, filteredSolutions);
			
			return filteredSolutions;
		}
		
		private void checkAsyncErrorsAndThrowIfNecessary() {
			if (asyncErrors == null)
				return;
			
			if (asyncErrors.size() == 1)
				throw Exceptions.normalizer(asyncErrors.get(0)).asRuntimeException();
			else {
				IllegalStateException exception = new IllegalStateException("Multiple errors occurred while resolving platform asset dependencies. Please check logs. First error:\n" + Exceptions.getEnsuredMessage(asyncErrors.get(0)));
				
				for (Throwable t: asyncErrors) {
					exception.addSuppressed(t);
				}
				
				throw exception;
			}
		}

		private class BuildOrderWalker {
			Set<String> traversedSolutions = new HashSet<>();
			Set<String> processingSolutions = new HashSet<>();
			Deque<String> dependencyStack = new ArrayDeque<>();
			int index;

			
			public void determineBuildOrder(List<Dependency> dependencies) {
				for (Dependency dependency: dependencies) {
					Set<Solution> solutions = dependency.getSolutions();
					
					if (solutions.size() != 1) {
						throw new IllegalStateException("Could not resolve dependency: " + NameParser.buildName(dependency));
					}
					
					Solution solution = solutions.iterator().next();
					
					String solutionName = NameParser.buildName(solution);
					
					dependencyStack.push(solutionName);
					
					try {
						if (!processingSolutions.add(solutionName)) {
							StringBuilder sb = buildStackInfo(dependencyStack);
							throw new IllegalStateException("Detected a dependency loop: "+sb);
						}
						
						if (!traversedSolutions.add(solutionName)) {
							continue;
						}
						
						// traverse
						determineBuildOrder(solution.getDependencies());
						
						// register in build order
						orderedFullSolutions.add(solution);
						solution.setOrder(index++);
					}
					finally {
						processingSolutions.remove(solutionName);
						dependencyStack.pop();
					}
				}
			}
		}
		

		private void walk(Iterable<Dependency> dependencies) {
			runningTraversions.incrementAndGet();
			try {
				for (Dependency dependency: dependencies) {
					walk(ParallelResolvingContext.empty, dependency, null);
				}
			}
			finally {
				decrementRunningAndCheckForCompletion();
			}
		}
		
		private void walk(ParallelResolvingContext context, Dependency dependency, Solution requestor) {
			runningTraversions.incrementAndGet();
			
			
			Predicate<Identification> acculumativePredicate = respectExclusions ? Exclusions.predicate(dependency).negate() : (i) -> true;
			ParallelResolvingContext dependencyContext = new ParallelResolvingContext(context, acculumativePredicate);
			
			Promise<Solution> promise = resolvedDependencies.computeIfAbsent(RangedArtifacts.from(dependency), k -> {
				return submit(() -> {
					Solution solution = resolveDependency(dependencyContext, requestor, dependency);
					return solution;
				});
			});
			
			promise.get(new AsyncCallback<Solution>() {
				@Override
				public void onSuccess(Solution resolvedSolution) {
					try {
						if (resolvedSolution != null) {
							synchronized (resolvedSolution) {
								dependency.getSolutions().add(resolvedSolution);
								resolvedSolution.getRequestors().add(dependency);
							}
							
							if (requestor != null)
								registerDependencyRelation(requestor, resolvedSolution);
						}
					}
					finally {
						decrementRunningAndCheckForCompletion();
					}
				}

				
				@Override
				public void onFailure(Throwable t) {
					decrementRunningAndCheckForCompletion();
					notifyError(t);
				}


			});
		}
		
		private void registerDependencyRelation(Solution requestor, Solution resolvedSolution) {
			synchronized (dependencyTree) {
				dependencyTree.put(new ResultArtifact(requestor), new ResultArtifact(resolvedSolution));
			}
		}

		private void decrementRunningAndCheckForCompletion() {
			if (runningTraversions.decrementAndGet() == 0) {
				synchronized (runningMonitor) {
					runningMonitor.notify();
				}
			}
		}
		
		private void notifyError(Throwable t) {
			synchronized (asyncErrorMonitor) {
				if (asyncErrors == null) {
					asyncErrors = new ArrayList<>();
				}
				
				asyncErrors.add(t);
			}
		}
		
		private void walk(ParallelResolvingContext context, Solution solution) {
			String solutionName = NameParser.buildName(solution);
			
			if (filterSolutionBeforeVisit && !filterSolution(solution))
				return;
			
			if (!visitedSolutions.add(solutionName)) {
				return;
			}
			
			if (!filterSolutionBeforeVisit && !filterSolution(solution))
				return;
			
			logger.debug("traversing " + solutionName);
			
			pomReader.read(walkScopeId, solution);
			
			if (walkParentStructure) {
				Stream.concat(solution.getImported().stream(), Stream.of(solution.getResolvedParent()))
				.filter(s -> s != null)
				.forEach(s -> {					
					registerDependencyRelation(solution, s);
					walk(ParallelResolvingContext.empty, s);
				});
			}
			
			List<Dependency> dependencies = solution.getDependencies();
			
			for (Iterator<Dependency> iterator = dependencies.iterator(); iterator.hasNext();) {
				Dependency dependency = iterator.next();
				
				if (context.isRelevant(dependency) && solutionDependencyFilter.test(solution, dependency) && filterDependency(dependency)) {
					solutionDependencyVisitor.accept(solution, dependency);
					walk(context, dependency, solution);
				}
				else {
					iterator.remove();
				}
			}
			
			if (buildRanged) {
				ResultArtifact solutionArtifact = new ResultArtifact(solution);
				
				if (upperBound.apply(solutionArtifact) != BoundaryHit.none) {
					weedingEntryPoints.add(solutionArtifact);
				}
			}
		}
		
		private boolean filterSolution(Solution s) {
			return solutionFilter.test(s) && artifactFilter.test(RangedArtifacts.from(s));
		}
		
		private boolean filterDependency(Dependency d) {
			return dependencyFilter.test(d) && artifactFilter.test(RangedArtifacts.from(d));
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
		
		private Solution resolveDependency(ParallelResolvingContext context, Solution depender, Dependency dependency) {
			Set<Solution> solutions = dependencyResolver.resolveTopDependency(walkScopeId, dependency);
			
			if (solutions.isEmpty()) {
				unresolvedDependencies.add(dependency);
				String msg = "Could not find a solution for dependency " + NameParser.buildName(dependency)
						+ (depender != null ? " depended by " + NameParser.buildName(depender) : "");
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
			highestSolution.getRequestors().add( dependency);
			return identityManagedSolutions.computeIfAbsent(RangedArtifacts.from(highestSolution), k -> {
				walk(context, highestSolution);
				return highestSolution;
			});
		}
		
		private Solution getHighest(Set<Solution> curSolutions) {
			return curSolutions.stream().sorted((s1, s2) -> versionComparator.compare(s1.getVersion(), s2.getVersion())).findFirst().orElse(null);
		}

	}
}
