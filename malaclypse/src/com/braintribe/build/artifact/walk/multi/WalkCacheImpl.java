// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.braintribe.build.artifact.retrieval.multi.coding.DependencyWrapperCodec;
import com.braintribe.build.artifact.retrieval.multi.coding.SolutionWrapperCodec;
import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;

public class WalkCacheImpl implements WalkCache {

	private Map<Dependency,Dependency> dependencies = CodingMap.createHashMapBased( new DependencyWrapperCodec());
	private Map<Solution, Solution> solutions = CodingMap.createHashMapBased( new SolutionWrapperCodec());
	private Map<Dependency, Set<Dependency>> undeterminedDependencies = CodingMap.createHashMapBased( new DependencyWrapperCodec());
	
	
	@Override
	public void addUnDeterminedDependency(Dependency dependency) {
		Set<Dependency> deps = undeterminedDependencies.get(dependency);
		if (deps == null) {
			deps = new HashSet<Dependency>();
		}
		deps.add(dependency);
		undeterminedDependencies.put(dependency, deps);
	}
	@Override
	public Set<Dependency> getUnDeterminedDependencies(Dependency dependency) {
		return undeterminedDependencies.get(dependency);
	}
	@Override
	public Set<Dependency> getCollectedUnDeterminedDependencies() {	
		Set<Dependency> result = new HashSet<Dependency>();
		for (Set<Dependency> deps : undeterminedDependencies.values()) {
			result.addAll( deps);
		}
		return result;
	}
	
	@Override
	public void addDependency(Dependency dependency) {
		dependencies.put( dependency, dependency);		
	}
	@Override
	public Dependency containsDependency(Dependency dependency) {		
		return dependencies.get(dependency);
	}	
	@Override
	public Set<Dependency> getCollectedDependencies() {
		return new HashSet<Dependency>(dependencies.values());
	}

	@Override
	public void addSolution(Solution solution) {
		solutions.put(solution, solution);		
	}
	@Override
	public Solution containsSolution(Solution solution) {
		return solutions.get(solution);
	}	
	@Override
	public Set<Solution> getCollectedSolutions() {
		return new HashSet<Solution>(solutions.values());		
	}

}
