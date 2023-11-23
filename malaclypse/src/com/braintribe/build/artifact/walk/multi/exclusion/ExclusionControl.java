// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi.exclusion;

import java.util.Set;
import java.util.Stack;

import com.braintribe.model.artifact.Exclusion;
import com.braintribe.model.artifact.Identification;


/**
 * stack based container to note exclusions 
 * @author Pit
 *
 */
public class ExclusionControl {

	private Stack<ExclusionContainer> exclusionStack = new Stack<ExclusionContainer>();
	private ExclusionContainer globalExclusions;
	private ExclusionContainerFactory containerFactory = new ExclusionContainerFactory();
	
	public ExclusionControl(Set<Exclusion> initialExclusions) throws ExclusionControlException{		
		if (initialExclusions != null) {
			try {
				globalExclusions = containerFactory.apply(initialExclusions);
			} catch (RuntimeException e) {
				throw new ExclusionControlException(e);
			}					
		}		
	}
	 
	public void push( Set<Exclusion> exclusions) throws ExclusionControlException{		
		try {			
			ExclusionContainer container = containerFactory.apply(exclusions);			
			exclusionStack.push( container);							
		} catch (RuntimeException e) {
			throw new ExclusionControlException(e);
		}
	}
	
	public void pop() {
		exclusionStack.pop();
	}
	
	public boolean isExcluded( Identification identification) {
		if (globalExclusions != null && globalExclusions.contains(identification))
			return true;
		if (!exclusionStack.isEmpty()) {
			int size = exclusionStack.size();
			if (size == 1) {
				return exclusionStack.peek().contains(identification);
			}
			for (int i = size-1; i >= 0; i--) {			
				if (exclusionStack.get(i).contains(identification)) {
					return true;
				}
			}			
		}
		return false;		
	}	
		
}
