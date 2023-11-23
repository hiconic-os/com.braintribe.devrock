// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.artifact.walk.multi.clash;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.walk.multi.clash.listener.ClashResolverNotificationListener;
import com.braintribe.build.artifact.walk.multi.clash.merger.DependencyMerger;
import com.braintribe.build.artifact.walk.multi.clash.merger.listener.DependencyMergerNotificationListener;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ResolvingInstant;


/**
 * a clash resolver and weeder rolled into one<br/>
 * uses an optimistic sorting logic to choose the winning dependency in a clash, and 
 * then weeds the dropped dependencies instantly from the dependency tree 
 * 
 * @author Pit
 *
 */
public abstract class AbstractWeedingClashResolver implements ConfigurableClashResolver, ClashResolverNotificationListener {
	private static Logger log = Logger.getLogger(AbstractWeedingClashResolver.class);

	private Stack<Dependency> dependencyWeedingStack;
	private Stack<Solution> solutionWeedingStack;
	private Set<ClashResolverNotificationListener> listeners = new HashSet<ClashResolverNotificationListener>();
	private DependencyMerger dependencyMerger;
	private InitialDependencyPrecedenceSorter initialPrecedenceSorter;
	protected boolean leniency = true;
	
	private Map<String, Map<String, Dependency>> contextToKnownDependenciesMap = new HashMap<String, Map<String, Dependency>>();
	private Map<String, Map<Dependency, List<Dependency>>> contextToClashesMap = new HashMap<String, Map<Dependency, List<Dependency>>>();
	private ResolvingInstant resolvingInstant = ResolvingInstant.posthoc;

	@Configurable
	public void setResolvingInstant(ResolvingInstant resolvingInstant) {
		this.resolvingInstant = resolvingInstant;
	}

	
	@Override @Configurable @Required
	public void setTerminalArtifactProvider(Supplier<Artifact> provider) {

	}
	
	@Override @Configurable
	public void setDependencyMerger(DependencyMerger dependencyMerger) {
		this.dependencyMerger = dependencyMerger;
	}
	
	
	@Override @Configurable @Required
	public void setInitialPrecedenceSorter(InitialDependencyPrecedenceSorter sorter) {
		initialPrecedenceSorter = sorter;		
	}
	

	@Override @Configurable
	public void setLeniency(boolean leniency) {
		this.leniency = leniency;
		
	}

	
	@Override
	public void addListener(ClashResolverNotificationListener listener) {		
		synchronized (listeners) {
			listeners.add( listener);	
			// delegate to merger 
			if (listener instanceof DependencyMergerNotificationListener && dependencyMerger != null)  {
				dependencyMerger.addListener((DependencyMergerNotificationListener) listener);
			}
		}
	}

	@Override
	public void removeListener(ClashResolverNotificationListener listener) {
		synchronized ( listeners) {					
			listeners.remove(listener);			
			// delegate to merger
			if (listener instanceof DependencyMergerNotificationListener && dependencyMerger != null) {
				dependencyMerger.removeListener( (DependencyMergerNotificationListener) listener);
			}
		}
	}

	
	/**
	 * returns a {@link List} of all {@link Dependency} that shared the same {@link Identification}
	 * @param suspect - the {@link Dependency} to act as template 
	 * @param input - the {@link List} of {@link Dependency} to scan
	 * @return - a {@link List} of all {@link Dependency} with a matching {@link Identification}
	 */
	public List<Dependency> extractClashingDependencies( Dependency suspect, List<Dependency> input) {
		List<Dependency> result = new ArrayList<Dependency>();
		for (Dependency dependency : input) {
			// don't add ourself
		
			if (ArtifactProcessor.identificationEquals( suspect, dependency)) 
				result.add( dependency);
		}
		
		return result;
	}

	
	/* (non-Javadoc)
	 * @see com.braintribe.build.artifact.walk.processing.clash.ClashResolver#resolveDependencyClashes(java.util.Collection)
	 */
	@Override
	public List<Dependency> resolveDependencyClashes( String walkScopeId, Solution terminal, Collection<Dependency> dependencies) throws ClashResolvingException {
		switch (resolvingInstant) {
		case adhoc:
			acknowledgeAdhocDependencyClashes(walkScopeId);
			return new ArrayList<Dependency>( dependencies);				
		case posthoc:	
		default:
			return posthocClashing(walkScopeId, terminal, dependencies);		
		}
	}
	
	private void dump( Collection<Dependency> deps) {
		String exp = deps.stream().map( d -> NameParser.buildName(d)).collect(Collectors.joining(","));
		System.out.println(exp);
	}
	
	private List<Dependency> sort( Collection<Dependency> deps) {
		List<Dependency> result = new ArrayList<>(deps);
		result.sort( new Comparator<Dependency>() {

			@Override
			public int compare(Dependency o1, Dependency o2) {
				String a = NameParser.buildName( o1);
				String b = NameParser.buildName( o2);
				return a.compareTo(b);
			}
			
		});
		return result;
	}

	private List<Dependency> posthocClashing(String walkScopeId, Solution terminal, Collection<Dependency> dependencies) throws ClashResolvingException {
		List<Dependency> clearedDependencies = new ArrayList<Dependency>();
		
		List<Dependency> droppedDependencies = new ArrayList<Dependency>();
		List<Dependency> dependencySortedAccordingRelevancy = new ArrayList<>(initialPrecedenceSorter.sortDependencies(dependencies)); // sort( dependencies); 
		Set<Dependency> mergedSources = new HashSet<Dependency>();
		List<Dependency> weedStartingPoints = new ArrayList<Dependency>();
		
		Map<Dependency, List<Dependency>> clashReportingMap = new HashMap<Dependency, List<Dependency>>();
		
		for (Dependency current : dependencySortedAccordingRelevancy) {
			
			acknowledgeDependencyClashResolving(walkScopeId, current);
			
			if (ArtifactProcessor.contains(droppedDependencies, current)) {
				if (log.isDebugEnabled()) {
					log.debug("skipping already dropped dependency [" + NameParser.buildName( current) + "]");
				}
				continue;
			}
			boolean alreadyProcessed = false;
			for (Identification id : mergedSources) {
				if (ArtifactProcessor.identificationEquals(id, current)) {
					if (log.isDebugEnabled()) {
						log.debug("skipping already merged dependency [" + NameParser.buildName( current) + "]");
					}
					alreadyProcessed = true;
					break;
				}					
			}
			if (alreadyProcessed)
				continue;
		
			List<Dependency> preMergedClashes = extractClashingDependencies(current, dependencySortedAccordingRelevancy);
			// check whether we still have to clash the one - remove weeded dependencies.. 
			preMergedClashes.removeAll( droppedDependencies);		
			// 

			// see whether the clashes can be reconciled (i.e. the dependency merged)
			List<Dependency> clashes;
			if (dependencyMerger != null) {
				clashes = dependencyMerger.mergeDependencies( walkScopeId, preMergedClashes, mergedSources);
			}
			else {
				clashes = preMergedClashes;
			}
			
			// less than two dependencies -> no clashes 
			if (clashes.size() < 2) {
				// add to cleared list
				if (clashes.size() > 0) {
					Dependency e = clashes.get(0);
					if (!clearedDependencies.contains( e)) {
						clearedDependencies.add( e);
					}
					else {
						log.trace( NameParser.buildName(e) + " already present");
					}
				} else {
					if (!clearedDependencies.contains( current)) {
						clearedDependencies.add(current);
					}
					else {
						log.trace( "current " + NameParser.buildName(current) + " already present");
					}
				}
				if (log.isDebugEnabled()) {
					log.debug("no clashes for dependency [" + NameParser.buildName( current) + "]");
				}
				continue;
			}
			
			// if not lenient, clashes may lead to exceptions 
			if (!leniency)  {		
				String msg = "clash detected on artifact [" + current.getGroupId() + ":" + current.getArtifactId() + "]";
				throw new ClashResolvingException( msg);											
			}
			
			//dump( clashes);
			// get the dominant dependency (either actually declared as dominant in the artifact or via sorting acc version)
			Dependency dominantDependency = getDominantDependency(terminal, clashes);
			if (dominantDependency == null) {
				// no dominant declared -> sort now 
				sort(clashes);
				dominantDependency = clashes.get( clashes.size()-1);
				if (log.isDebugEnabled()) {
					log.debug("determined dominant dependency [" + NameParser.buildName(dominantDependency) + "] per highest version sort");
				}
			} else {
				// was declared dominant
				if (log.isDebugEnabled()) {
					log.debug("determined dominant dependency [" + NameParser.buildName(dominantDependency) + "] per dominant property");
				}
			}
			// add to cleared list 
			if (!clearedDependencies.contains( dominantDependency)) {
				clearedDependencies.add( dominantDependency);
			}
			else {
				log.trace( NameParser.buildName( dominantDependency) + " already present");
			}
			// drop dominant from clashes 
			clashes.remove(dominantDependency);
			
			// 
			clashReportingMap.put(dominantDependency, clashes);
			
			dependencyWeedingStack = new Stack<Dependency>();
			solutionWeedingStack = new Stack<Solution>();
			
			// rewire the requesters and weed the dropped dependency		
			Set<Artifact> requesters = dominantDependency.getRequestors();
			for (Dependency clash : clashes) {
				// rewire
				for (Artifact requester : clash.getRequestors()) {
					// replace the dominant dependency in the dependencies of the requester of the clashed dependencies
					Collection<Dependency> artifactsDependencies = requester.getDependencies();
					ArtifactProcessor.coarseDependencyRemove( artifactsDependencies, clash);
					artifactsDependencies.add( dominantDependency);
					requesters.add( requester);
				}
				clash.setOverridingDependency( dominantDependency);
				// weed and add to dropped dependencies  
				weedStartingPoints.add( clash);
				Set<Dependency> weededDependency = weedDependency(clash);
				droppedDependencies.addAll( weededDependency);
			}						
		}
		//
		if (log.isDebugEnabled() && !mergedSources.isEmpty()) {		
			log.debug( String.format("Removing [%d] merged sources from remaining dependencies", mergedSources.size()));						
		}
		
		clearedDependencies.removeAll(mergedSources);
		
		if (!droppedDependencies.isEmpty()) {
			
			for (Dependency dependency : droppedDependencies) {
				if (log.isDebugEnabled()) {
					log.debug( "weeding : dropping [" +  NameParser.buildName(dependency) + "]");
				}
				clearedDependencies.remove( dependency);
			}			
		}
		// 
		// post process for clash reporting
		//
		// remove all clashes that are dropped
		Map<Dependency, Set<Dependency>> dependencyToClashesToDropMap = new HashMap<Dependency, Set<Dependency>>();
		
		for (Entry<Dependency, List<Dependency>> entry : clashReportingMap.entrySet()) {
		
			Iterator<Dependency> iterator = entry.getValue().iterator();
			while (iterator.hasNext()) {
				Dependency clash = iterator.next();
				if (weedStartingPoints.contains(clash)) {
					continue;
				}
				
				Set<Artifact> requestors = clash.getRequestors();
				for (Artifact requester : requestors) {
					Solution requestingSolution = (Solution) requester;
					boolean removeClash = true;
				
					// if at least one of the requesting dependencies has not been dropped, we must report the clash
					Iterator<Dependency> requesterIterator = requestingSolution.getRequestors().iterator(); 
					while (requesterIterator.hasNext()){
						Dependency requestingDependency = requesterIterator.next();
						if (!droppedDependencies.contains( requestingDependency)) {
							removeClash = false;						
						}						
						else {
							requesterIterator.remove();
						}
					}
					if (removeClash) {
						Set<Dependency> clashesToDrop = dependencyToClashesToDropMap.get( entry.getKey());
						if (clashesToDrop == null) {
							clashesToDrop = new HashSet<Dependency>();
							dependencyToClashesToDropMap.put(entry.getKey(), clashesToDrop);							
						}
						clashesToDrop.add(clash);		
					}
				}
			}
		}

		// acknowledge remaining clashes
		for (Entry<Dependency, List<Dependency>> entry : clashReportingMap.entrySet()) {			
			List<Dependency> clashes = entry.getValue();
			Set<Dependency> clashesToDrop = dependencyToClashesToDropMap.get(entry.getKey());
			if (clashesToDrop != null) {
				clashes.removeAll(clashesToDrop);
			}
			if (clashes.size() > 0) {
				if (log.isDebugEnabled()) {
					log.debug("notifying clash on [" + NameParser.buildName(entry.getKey()) + "]");
				}
				// must remove any requestors that have been dropped
				acknowledgeDependencyClashes(walkScopeId,  entry.getKey(), clashes);
			}
		}
		acknowledgeAdhocDependencyClashes( walkScopeId);
		// drop duplicates?? 
		return clearedDependencies;
	}
	

	/* (non-Javadoc)
	 * @see com.braintribe.build.artifact.walk.processing.clash.ClashResolver#resolveSolutionClashes(java.util.Collection)
	 */
	@Override
	public List<Solution> resolveSolutionClashes(String walkScopeId, Collection<Solution> solutions) throws ClashResolvingException {		
		List<Solution> clearedSolutions = new ArrayList<Solution>( solutions.size());
		for (Solution solution : solutions) {
			List<Solution> clashes = new ArrayList<Solution>();
			for (Solution suspect : solutions) {
				if (solution == suspect)
					continue;
				if (ArtifactProcessor.identificationEquals( solution, suspect)) {
					// clash detected..	
					if (log.isDebugEnabled()) {
						log.debug("clash found: [" + NameParser.buildName( solution) + "] vs [" + NameParser.buildName( suspect) + "]");	
					}
					clashes.add( suspect);
				}
			}
			if (clashes.size() == 0) {
				if (ArtifactProcessor.contains(clearedSolutions, solution) == false)
					clearedSolutions.add( solution);
				continue;
			}
			clashes.add( solution);
			if (log.isDebugEnabled()) {
				log.debug( "optimistically resolving [" + clashes.size() + "] clashes");
			}
			Collections.sort( clashes, new Comparator<Solution>() {

				@Override
				public int compare(Solution o1, Solution o2) {					
					if (VersionProcessor.isHigher( o1.getVersion(), o2.getVersion()))
						return 1;
					if (VersionProcessor.isLess( o1.getVersion(), o2.getVersion()))
						return -1;
					return 0;
				}
				
			});
			// add the highest..
			Solution solToUse = clashes.get( clashes.size()-1);
			if (log.isDebugEnabled()) {
				log.debug( "using solution [" + NameParser.buildName( solToUse) + "]");
			}
			if (ArtifactProcessor.contains(clearedSolutions, solToUse) == false)
				clearedSolutions.add( solToUse);
			// broadcast
			acknowledgeSolutionClashes(walkScopeId, solToUse, clashes);
			
		}
		return clearedSolutions;
	}


	/**
	 * check if the dependency matches a dominant declaration 
	 * @param dominants - {@link Set} of {@link Identification} that declare the dominants 
	 * @param dependency - the {@link Dependency} to check against
	 * @return - true if {@link Dependency} matches a dominant, false otherwise
	 */
	private boolean matchesDominantPattern( Set<Identification> dominants, Dependency dependency) {
		for (Identification identification : dominants) {
			if (ArtifactProcessor.identificationEquals(identification, dependency))
				return true;
		}
		return false;
	}
	
	
	/**
	 * retrieves the dominant {@link Dependency} from the terminal {@link Artifact} if any 
	 * @param dependencies - a {@link Collection} of {@link Dependency} that are to choose from  
	 * @return - the dominant {@link Dependency} or null if none's dominant
	 */
	public Dependency getDominantDependency( Artifact terminalArtifact, Collection<Dependency> dependencies) {
		
		Set<Identification> dominants = terminalArtifact.getDominants();
		if (
				(dominants != null) &&
				(dominants.size() > 0)
			) {
			for (Dependency dependency : dependencies) {
				if ( matchesDominantPattern( dominants, dependency) == false) {
					continue;
				}
				// check if it's our dependency (might be merged??)
				for (Artifact artifact : dependency.getRequestors()) {
					// if the terminal's one of the requesters, it's ours 
					if (ArtifactProcessor.artifactEquals(terminalArtifact, artifact)) {
						if (log.isDebugEnabled()) {
							log.debug("Determined dependency [" + NameParser.buildName(dependency) + "] to be dominant");
						}
						return dependency;
					}
				}				
			}
		}
		return null;
	}
	
	/**
	 * sort the clashes.. it is expect that the winner is the LAST in the list after the sort
	 * @param clashes - a {@link List} of {@link Dependency} to sort according the {@link ClashResolver}'s logic
	 */
	protected abstract void sort( List<Dependency> clashes);
	
	
	/**
	 * weeder for dependencies <br/>
	 * iterates over all {@link Solution}, and removes the {@link Dependency} from the requesters of the {@link Solution}<br/>
	 * if no requesters remain, the solution can be dropped -> weed is propagated to {@link #weedSolution(Solution)}
	 * @param dependency - the {@link Dependency} to weed
	 * @return - the {@link Set} containing the {@link Dependency} that were weeded out
	 */
	private Set<Dependency> weedDependency( Dependency dependency) {
		Set<Dependency> dropped = new HashSet<Dependency>();
		if (dependencyWeedingStack.contains(dependency)) {
			if (log.isDebugEnabled()) {
				log.debug("Stack check : dependency [" + NameParser.buildName(dependency) +"] is in the process of being weeded");
			}
			return dropped;
		}
		
		dependencyWeedingStack.push(dependency);
		
		try {
		
			Set<Solution> solutions = dependency.getSolutions(); 
			if (solutions == null)
				return dropped;
			for (Solution solution : solutions) {
				Set<Dependency> requesters = solution.getRequestors();
				if (requesters.remove(dependency) == false) {
					log.warn("Dependency [" + NameParser.buildName( dependency) + "] not found in solution [" + NameParser.buildName(solution) + "]'s requestor list");
				}
				if (requesters.size() == 0) {
					if (log.isDebugEnabled()) {
						log.debug("triggering weed on solution [" + NameParser.buildName(solution) + "]");
					}
					dropped.addAll( weedSolution( solution));
				}
			}
			dropped.add( dependencyWeedingStack.peek());
			return dropped;
		}
		finally {			
			dependencyWeedingStack.pop();
		}
		
	}
	
	/**
	 * weeder for solutions <br/>
	 * iterates over all {@link Dependency} of a {@link Solution}, and removes the {@link Solution} from the requesters of the {@link Dependency}<br/>
	 * if no requesters remain, the {@link Dependency} can be dropped -> weed is propagated to {@link #weedDependency(Dependency)}
	 * @param solution - the {@link Solution} to weed
	 * @return - the {@link Set} of {@link Dependency} that were weeded out as a consequence 
	 */
	private Set<Dependency> weedSolution( Solution solution) {
		Set<Dependency> dropped = new HashSet<Dependency>();
		if (solutionWeedingStack.contains(solution)) {
			if (log.isDebugEnabled()) {
				log.debug("Stack check : solution [" + NameParser.buildName(solution) +"] is in the process of being weeded");
			}
			return dropped;
		}
		solutionWeedingStack.push( solution);
		
		try {		
			List<Dependency> dependencies = solution.getDependencies();
			if (
					dependencies == null ||
					dependencies.size() == 0
				)
				return dropped;
			for (Dependency dependency : dependencies) {
				Set<Artifact> requesters = dependency.getRequestors();
				if (requesters.remove( solution) == false) {
					log.warn("Solution [" + NameParser.buildName( solution) + "] not found in dependency [" + NameParser.buildName( dependency) + "]'s requestor list");
				}
				if (requesters.size() == 0) {
					if (log.isDebugEnabled()) {
						log.debug( "triggering weed on dependency [" + NameParser.buildName(dependency) + "]");
					}
					dropped.addAll( weedDependency( dependency));
				}
			}
			return dropped;
		}
		finally {
			solutionWeedingStack.pop();
		}
	}

	@Override
	public void acknowledgeDependencyClashes(String walkScopeId, Dependency dependency, List<Dependency> clashes) {
		synchronized(listeners) {
			for (ClashResolverNotificationListener listener : listeners){
				listener.acknowledgeDependencyClashes( walkScopeId, dependency, clashes);	
			}
		}			
	}

	@Override
	public void acknowledgeSolutionClashes(String walkScopeId, Solution solution, List<Solution> clashes) {
		synchronized(listeners) {
			for (ClashResolverNotificationListener listener : listeners) {
				listener.acknowledgeSolutionClashes(walkScopeId, solution, clashes);
			}
		}		
	}

	@Override
	public void acknowledgeDependencyClashResolving(String walkScopeId, Dependency dependency) {
		synchronized(listeners) {
			for (ClashResolverNotificationListener listener : listeners) {
				listener.acknowledgeDependencyClashResolving(walkScopeId, dependency);
			}
		}			
	}
	
	@Override
	public void acknowledgeSolutionClashResolving(String walkScopeId, Solution solution) {
		synchronized(listeners) {
			for (ClashResolverNotificationListener listener : listeners) {
				listener.acknowledgeSolutionClashResolving(walkScopeId, solution);
			}
		}		
	}

	@Override
	public Dependency adhocClashResolving(String walkScopeId, Dependency dependency) {
		switch ( resolvingInstant) {
			case adhoc: 
				String identification = dependency.getGroupId() + ":" + dependency.getArtifactId();
				Map<String, Dependency> knownDependencies = contextToKnownDependenciesMap.get( walkScopeId);
				if (knownDependencies == null) {
					knownDependencies = new HashMap<>();
					contextToKnownDependenciesMap.put(walkScopeId, knownDependencies);
					knownDependencies.put( identification, dependency);					
				}
				else {				
					Dependency winner = knownDependencies.get( identification);
					if (winner != null) {
						if (log.isDebugEnabled()) {
							log.debug("adhoc clash : [" + NameParser.buildName(dependency)  + "](" + dependency.getPathIndex() + ") is replaced by [" + NameParser.buildName(winner) + "] (" + winner.getPathIndex() + ")");
						}
						// store 
						Map<Dependency, List<Dependency>> clashes = contextToClashesMap.get(walkScopeId);
						if (clashes == null) {
							clashes = new HashMap<Dependency, List<Dependency>>();
							contextToClashesMap.put(walkScopeId, clashes);
						}
						List<Dependency> overridden = clashes.get(winner);
						if (overridden == null) {
							overridden = new ArrayList<Dependency>();
							clashes.put(winner, overridden);
						}
						overridden.add(dependency);
						
						for (Artifact requester : dependency.getRequestors()) {
							// replace the dominant dependency in the dependencies of the requester of the clashed dependencies
							Collection<Dependency> artifactsDependencies = requester.getDependencies();
							ArtifactProcessor.coarseDependencyRemove( artifactsDependencies, dependency);
							artifactsDependencies.add( winner);
							
							winner.getRequestors().add( requester);
						}
						
						dependency.setOverridingDependency( winner);
						
						
						return winner;
					}
					knownDependencies.put( identification, dependency);					
				}	
				return null;
			case posthoc:
			default:
				return null;						
		}
	}

	/**
	 * for the adhoc clash resolvers : return information what clashed
	 * @param walkScopeId -
	 */
	protected void acknowledgeAdhocDependencyClashes(String walkScopeId) {
		Map<Dependency, List<Dependency>> clashes = contextToClashesMap.get(walkScopeId);
		if (clashes == null)
			return;
		for (Entry<Dependency, List<Dependency>> entry : clashes.entrySet()) {
			acknowledgeDependencyClashes(walkScopeId,  entry.getKey(), entry.getValue());
		}
		
	}
	
}
