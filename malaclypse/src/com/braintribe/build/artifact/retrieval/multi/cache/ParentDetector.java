// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.cache;

import java.util.List;
import java.util.Stack;

import com.braintribe.model.artifact.Solution;

public interface ParentDetector {
	public boolean isStoredParentSolution( String walkscope, Solution solution);	
	public void addParentSolutionToStore( String walkscope, Solution solution);	
	public List<Solution> getParentSequence( String walkscope);	
	public void addToParentSequence( String walkscope, Solution solution);
	
	public Stack<Solution> getParentStackForScope( String walkScope);
	public Stack<Solution> getLookupStackForScope( String walkScope);
}
