// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.coding.ArtifactWrapperCodec;
import com.braintribe.build.artifact.retrieval.multi.coding.DependencyWrapperCodec;
import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;

/**
 * a cache for the context (aka the walk scope id) 
 * @author pit
 *
 */
public class CacheContext {
	private static final int MAX_WAIT_LOOPS = 10;
	private static Logger log = Logger.getLogger(CacheContext.class);
	private Map<String, Solution> locationToSolutionMap = new ConcurrentHashMap<String, Solution>();
	private Map<Solution, Solution> keyToSolutionMap = CodingMap.createHashMapBased( new SolutionWrapperCodec());
	private Set<Solution> parents = CodingSet.createHashSetBased( new ArtifactWrapperCodec());
	private List<Solution> parentNavigationList = new ArrayList<Solution>();
	private Map<Solution, Solution> redirectionMap = CodingMap.createHashMapBased( new SolutionWrapperCodec());
	private Stack<Solution> parentStack = new Stack<Solution>();
	private Stack<Solution> lookupStack = new Stack<Solution>();
	
	private Map<Part, Solution> solutionByPart = new ConcurrentHashMap<Part, Solution>();
	private Map<String, Part> partByName = new ConcurrentHashMap<String, Part>();
	private Map<Dependency, Solution> dependencyToParentSolutionMap = CodingMap.createHashMapBased( new DependencyWrapperCodec());
	

	public void addToSolutionCache(Part part, Solution solution) {
		solutionByPart.put( part, solution);
	}


	public Solution getSolutionFromCache(Part part) {
		return solutionByPart.get( part);
	}


	public void addToPartCache(String name, Part part) {
		partByName.put( name, part);		
	}
	

	public void addToCache(Part part) {		
		partByName.put( NameParser.buildName(part), part);		
	}

	public Part getPartFromCache(String name) {
		return partByName.get( name);
	}


	public Collection<Part> getPartsFromCache() {
		return partByName.values();
	}


	public void clear() {				
		solutionByPart.clear();
		partByName.clear();
		locationToSolutionMap.clear();
		keyToSolutionMap.clear();		
	}

	

	public void addSolution(Solution artifact, String location) {
		locationToSolutionMap.put(location, artifact);		
		keyToSolutionMap.put( artifact, artifact);	
	}


	public Solution getSolution(String location) {		
		return locationToSolutionMap.get(location);
	}


	public Solution getSolution(String groupId, String artifactId,String version) {
		Solution solution = Solution.T.create();
		solution.setGroupId(groupId);
		solution.setArtifactId(artifactId);
		try {
			solution.setVersion( VersionProcessor.createFromString(version));
		} catch (VersionProcessingException e) {
			log.error("cannot lookup solution [" + groupId + ":" + artifactId +"#" + version + "]", e);
			return null;
		}
		return getSolution(solution);
	}


	public Solution getSolution(Solution solution) {
		return keyToSolutionMap.get(solution);
	}
	
	public boolean isStoredParentSolution(Solution solution) {
		return parents.contains(solution);
	}
	
	public void addParentSolutionToStore( Solution solution) {
		parents.add(solution);
	}
	
	public List<Solution> getParentSequence() {
		return parentNavigationList;
	}	
	public void addToParentSequence( Solution solution) {
		parentNavigationList.add(solution);
	}
	
	
	public synchronized void addToDependencyToParentMap( Dependency dependency, Solution parent) {
		if (!dependencyToParentSolutionMap.containsKey(dependency)) {
			dependencyToParentSolutionMap.put(dependency, parent);
		}
	}
	
	public synchronized Solution getParentFromDependencyToParentMap( Dependency dependency) {
		Solution solution = dependencyToParentSolutionMap.get(dependency); 
		if (solution == null)
			return null;				
		if (!solution.getResolved()) {
			int i = 0;
			do {
				try {
					Thread.sleep(100);
					i++;
				} catch (InterruptedException e) {
					;
				}
				if (!solution.getResolved() && i > MAX_WAIT_LOOPS) {
					log.error("found solution [" + NameParser.buildName(solution) + "] to [" + NameParser.buildName(dependency) + "] is still not resolved ");
					return null;
				}
			
			} while (!solution.getResolved());
			
		}
		return solution; 				
	}
	
	public void addToRedirectionMap( Solution solution, Solution redirection) {
		redirectionMap.put(solution, redirection);
	}
	
	public Solution getRedirection( Solution solution) {
		return redirectionMap.get(solution);
	}
	
	public Stack<Solution> getParentStack() {
		return parentStack;
	}	
	public Stack<Solution> getLookupStack() {
		return lookupStack;
	}
}
 
