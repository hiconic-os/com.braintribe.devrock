// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;

/**
 * a cache for all pom readers in the runtime scope, uses the contextId to separate walk ids between them 
 * @author pit
 *
 */
public class CacheInstance implements Cache{

	private Map<String, CacheContext> contextToCacheMap = new ConcurrentHashMap<String, CacheContext>();
	
	private CacheContext getContext( String contextId) {
		CacheContext context = contextToCacheMap.get( contextId);
		if (context ==  null) {
			context = new CacheContext();
			contextToCacheMap.put(contextId, context);
		}
		return context;
	}

	@Override
	public void addToSolutionCache(String contextId, Part part, Solution solution) {
		getContext(contextId).addToSolutionCache(part, solution);
		
	}

	@Override
	public Solution getSolutionFromCache(String contextId, Part part) {
		return getContext(contextId).getSolutionFromCache(part);
	}

	@Override
	public void addToPartCache(String contextId, String name, Part part) {
		getContext(contextId).addToPartCache(name, part);		
	}

	@Override
	public void addToCache(String contextId, Part part) {
		getContext(contextId).addToCache(part);
		
	}

	@Override
	public Part getPartFromCache(String contextId, String name) {
		return 	getContext( contextId).getPartFromCache(name);
	}

	@Override
	public Collection<Part> getPartsFromCache(String contextId) {
		return getContext(contextId).getPartsFromCache();
	}

	@Override
	public void addSolution(String contextId, Solution solution, String location) {
		getContext(contextId).addSolution(solution, location);		
	}

	@Override
	public Solution getSolution(String contextId, String location) {		
		return getContext(contextId).getSolution(location);
	}

	@Override
	public Solution getSolution(String contextId, String groupId, String artifactId, String version) {
		return getContext(contextId).getSolution(groupId, artifactId, version);
	}

	@Override
	public Solution getSolution(String contextId, Solution solution) {
		return getContext(contextId).getSolution(solution);
	}

	@Override
	public void clear(String contextId) {
		getContext(contextId).clear();		
	}

	@Override
	public void clear() {
		for (CacheContext context : contextToCacheMap.values()) {
			context.clear();
		}
	}

	@Override
	public boolean isStoredParentSolution( String contextId, Solution solution) {
		return getContext( contextId).isStoredParentSolution(solution);
	}
	
	@Override
	public void addParentSolutionToStore( String contextId, Solution solution) {
		getContext( contextId).addParentSolutionToStore(solution);
	}
	
	@Override
	public List<Solution> getParentSequence( String contextId) {
		return getContext( contextId).getParentSequence();
	}
	
	@Override
	public void addToParentSequence( String contextId, Solution solution) {
		getContext( contextId).addToParentSequence(solution);
	}

	@Override
	public Solution getResolvedParentSolution(String contextId, Dependency dependency) {
		return getContext(contextId).getParentFromDependencyToParentMap(dependency);
	}

	@Override
	public void addResolvedParentSolution(String contextId, Dependency dependency, Solution solution) {
		getContext(contextId).addToDependencyToParentMap(dependency, solution);		
	}

	@Override
	public void addRedirection(String contextId, Solution solution, Solution redirection) {
		getContext(contextId).addToRedirectionMap(solution, redirection);
		
	}

	@Override
	public Solution getRedirection(String contextId, Solution solution) {
		return getContext(contextId).getRedirection(solution);
	}

	@Override
	public Stack<Solution> getParentStackForScope(String contextId) {
		return getContext(contextId).getParentStack();		
	}

	@Override
	public Stack<Solution> getLookupStackForScope(String contextId) {
		return getContext(contextId).getLookupStack();
	}

	
	


}
