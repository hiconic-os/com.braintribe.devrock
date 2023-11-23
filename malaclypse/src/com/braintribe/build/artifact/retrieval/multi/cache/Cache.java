// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================


package com.braintribe.build.artifact.retrieval.multi.cache;

import java.util.Collection;

import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;

/**
 * a context sensitive cache for the pom reader 
 * 
 * @author pit
 *
 */
public interface Cache extends ParentDetector{

	public void addToSolutionCache( String contextId, Part part, Solution solution);
	public Solution getSolutionFromCache( String contextId, Part part);
	
	public void addToPartCache( String contextId, String name, Part part);
	public void addToCache( String contextId, Part part);
	public Part getPartFromCache( String contextId, String name);	
	public Collection<Part> getPartsFromCache(String contextId);
	
	void addSolution( String contextId, Solution solution, String location);	
	Solution getSolution( String contextId, String location);
	Solution getSolution( String contextId, String groupId, String artifactId, String version);
	Solution getSolution( String contextId, Solution solution);
	
	void addResolvedParentSolution( String contextId, Dependency dependency, Solution solution);
	Solution getResolvedParentSolution( String contextId, Dependency dependency);
	
	void addRedirection( String contextId, Solution solution, Solution redirection);
	Solution getRedirection( String contextId, Solution solution);
	
	public void clear(String contextId);
	public void clear();
	
	
}
