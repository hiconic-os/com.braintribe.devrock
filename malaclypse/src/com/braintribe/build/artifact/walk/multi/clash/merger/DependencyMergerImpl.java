// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi.clash.merger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.cache.SolutionWrapperCodec;
import com.braintribe.build.artifact.walk.multi.clash.merger.listener.DependencyMergerNotificationListener;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;

/**
 * dependency merger: merges all dependencies within a collection<br/>
 * any merged dependencies are removed from the collection and replaced with the merged dependency
 * 
 * @author pit
 *
 */
public class DependencyMergerImpl implements DependencyMerger, DependencyMergerNotificationListener{
	private static Logger log = Logger.getLogger(DependencyMergerImpl.class);	
	private Set<DependencyMergerNotificationListener> listeners;
	
	@Override @Configurable
	public void addListener( DependencyMergerNotificationListener listener) {
		if (listeners == null)
			listeners = new HashSet<DependencyMergerNotificationListener>();
		listeners.add(listener);
	}
	@Override @Configurable
	public void removeListener( DependencyMergerNotificationListener listener) {
		if (listeners == null)
			return;
		listeners.remove( listener);
		if (listeners.isEmpty())
			listeners = null;
	}
	
	/* (non-Javadoc)
	 * @see com.braintribe.build.artifact.walk.multi.clash.merger.DependencyMerger#mergeDependencies(java.lang.String, java.util.Collection, java.util.Collection)
	 */
	@Override
	public List<Dependency> mergeDependencies(String walkScopeId,  Collection<Dependency> dependencies, Collection<Dependency> droppedDependencies) {
		List<Dependency> result = new ArrayList<Dependency>();
		result.addAll( dependencies);
		
		List<Dependency> sortedDependencies = sortDependenciesByUpperBounds(dependencies);
		Set<Dependency> mergedDependencies = new HashSet<Dependency>();
		boolean merged;
		do {
			merged = false;
			for (Dependency one : sortedDependencies) {				
				for (Dependency two : sortedDependencies) {
					if (two == one)
						continue;
					Dependency mergedDependency = mergeDependency(one, two);
					if (mergedDependency != null) {						
						// add new dependency to result 
						result.add( mergedDependency);
						// add to dropped - so that they can be removed outside
						droppedDependencies.add( one);
						droppedDependencies.add( two);
						// remove the merged dependencies from list 
						sortedDependencies.remove(one);
						sortedDependencies.remove(two);
						// remove the merged from result (might be a merged dependencies from an earlier iteration)
						result.remove(one);
						result.remove( two);
						// set flag and stop 
						merged = true;
						// one or two might be already merged 
						mergedDependencies.remove( one);
						mergedDependencies.remove( two);
						mergedDependencies.add(mergedDependency);
						if (log.isDebugEnabled()) {
							log.debug( String.format("[%s] has been merged from [%s] and [%s]", NameParser.buildName(mergedDependency), NameParser.buildName(one), NameParser.buildName(two)));
						}
						// add merged to list of dependency merge candidates 
						sortedDependencies.add( mergedDependency);
						break;
					}
				}
				// if we were able to merge, stop 
				if (merged)
					break;
			}
		} while (merged); // do as long as we were able to merge 
		
		//
		// monitor.. 
		//
		if (mergedDependencies.size() > 0) {
			acknowledgeMerges(walkScopeId, mergedDependencies);
		}
		return result;
	}
	
	/**
	 * merge a dependency - creates a new dependency if successful and merges requesters, solutions 
	 * @param one - the first {@link Dependency}
	 * @param two - the second {@link Dependency}
	 * @return - the merged {@link Dependency} or null if the dependencies couldn't be merged.
	 */
	private Dependency mergeDependency( Dependency one, Dependency two) {
		VersionRange firstRange = one.getVersionRange();
		VersionRange secondRange = two.getVersionRange();
		// for now, direct match ranges are not considered being "proper" ranges, even if one could argue that they are 
		// a collapsed form of a range. It has shown that the merger produces better (maven expected) results, if it only merges "real" ranges, and considers
		// a range and a direct version as a clash. 
		// pit, 2.2017
		if (!firstRange.getInterval() || !secondRange.getInterval())
			return null;
		
		try {					
			
			VersionRange mergedRange = VersionRangeProcessor.merge(firstRange, secondRange);
			if (mergedRange != null) {
				firstRange = mergedRange;										

				Dependency mergedDependency = Dependency.T.create();
				mergedDependency.setVersionRange( mergedRange);				
				ArtifactProcessor.transferIdentification(mergedDependency, one);				

				// merge parents
				Set<Dependency> mergedParentDependencies = new HashSet<Dependency>();				
				mergedParentDependencies.add( one);			
				mergedParentDependencies.add( two);
				mergedDependency.setMergeParents( mergedParentDependencies);
				
				// merge requesters
				Set<Artifact> mergedRequestors = new HashSet<Artifact>();
				if (one.getRequestors() != null)
					mergedRequestors.addAll( one.getRequestors());
				if (two.getRequestors() != null)
					mergedRequestors.addAll( two.getRequestors());						
				mergedDependency.setRequestors(mergedRequestors);
								
				
				// filter (and merge) solutions 
				joinSolutions(mergedDependency, one.getSolutions(), two.getSolutions());		
				return mergedDependency;
			}
			// 
		} catch (VersionProcessingException e) {
			String msg = "cannot merge ranges [" + VersionRangeProcessor.toString(firstRange) + "] and [" + VersionRangeProcessor.toString(secondRange) + "] as " + e;
			log.error( msg, e);			
		}
		return null;
	}
	
	/**
	 * filter solutions of the two merge sources, and combine (only one solution remains if they're the same version)
	 * @param mergedDependency - the {@link Dependency} that is merged
	 * @param candidateOnesSolutions - the {@link Set} of {@link Solution} of one source 
	 * @param candidateTwosSolutions - the {@link Set} of {@link Solution} of the other source 
	 * @return - the merged {@link Dependency}
	 */
	private Dependency joinSolutions( Dependency mergedDependency, Set<Solution> candidateOnesSolutions, Set<Solution> candidateTwosSolutions) {
		Set<Solution> filteredOne = filterSolutions( mergedDependency.getVersionRange(), candidateOnesSolutions);
		Set<Solution> filteredTwo = filterSolutions( mergedDependency.getVersionRange(), candidateTwosSolutions);
		
		Set<Solution> solutions = CodingSet.createHashSetBased( new SolutionWrapperCodec());
				
		solutions.addAll(filteredOne);
		solutions.addAll(filteredTwo);
		
		mergedDependency.getSolutions().addAll(solutions);
		return mergedDependency;
		
	}
	
	/**
	 * filter the set of solutions according the range 
	 * @param range - the {@link VersionRange} to filter with 
	 * @param candidates - the {@link Set} of {@link Solution} from the source 
	 * @return - a {@link Set} of {@link Solution} that match the range 
	 */
	private Set<Solution> filterSolutions( VersionRange range, Set<Solution> candidates) {
		Set<Solution> solutions = new HashSet<Solution>();
		if (candidates == null)
			return solutions;
		for (Solution candidate : candidates) {
			Version candidateVersion = candidate.getVersion();
			if (VersionRangeProcessor.matches(range, candidateVersion)) {
				solutions.add( candidate);
			}
			
		}
		return solutions;
	}
	
	/**
	 * higher upper bounds, lower position 
	 * @param dependencies - the dependencies to sort 
	 * @return - the sorted dependencies 
	 */
	private List<Dependency> sortDependenciesByUpperBounds( Collection<Dependency> dependencies) {
		List<Dependency> result = new ArrayList<Dependency>( dependencies);
		
		Collections.sort(result, new Comparator<Dependency>() {

			@Override
			public int compare(Dependency o1, Dependency o2) {
				VersionRange range1 = o1.getVersionRange();
				VersionRange range2 = o2.getVersionRange();
				if (Boolean.TRUE.equals( range1.getInterval())) {
					Version maximum1 = range1.getMaximum();
					// if maximum is null, then there's no upper bound, so this version does win, no matter which one 
					if (maximum1 == null) {
						return -1;
					}
					if (Boolean.TRUE.equals( range2.getInterval())) {
						Version maximum2 = range2.getMaximum();
						if (VersionProcessor.isHigher( maximum1, maximum2))
							return -1;
						else
							return 1;
					}
					else {
						Version version = range2.getDirectMatch();
						if (VersionProcessor.isHigher(maximum1, version))
							return -1;
						else
							return 1;
					}
				}
				else {
					Version version1 = range1.getDirectMatch();
					if (Boolean.TRUE.equals( range2.getInterval())) {
						Version maximum2 = range2.getMaximum();
						if (maximum2 == null) {
							return 1;
						}
						if (VersionProcessor.isHigher(version1, maximum2))
							return -1;
						else
							return 1;
					}
					else {
						Version version2 = range2.getDirectMatch();
						if (VersionProcessor.isHigher(version1, version2))
							return -1;
						else
							return 1;
					}
				}		
			}
			
		});
		return result;
	}
	@Override
	public void acknowledgeMerges(String walkScopeId, Set<Dependency> mergedDependencies) {
		if (listeners != null) {
			for (DependencyMergerNotificationListener listener : listeners) {
				listener.acknowledgeMerges( walkScopeId, mergedDependencies);
			}
		}
	}
	
	
}
