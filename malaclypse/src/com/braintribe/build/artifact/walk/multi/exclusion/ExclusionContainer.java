// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi.exclusion;

import java.util.Set;

import com.braintribe.model.artifact.Identification;

/**
 * a container for identifications that should be excluded during a walk <br/>
 * implementations can be fast, see {@link SetBasedExclusionContainer} or slow, but support wildcards, see {@link ListBasedExclusionContainer}<br/>
 * see the {@link ExclusionContainerFactory} that creates these types 
 * 
 * @author Pit
 *
 */
public interface ExclusionContainer {
	/**
	 * checks whether the dependency (identification part of it) is to be excluded from parsing 
	 * @param identification - the {@link Identification} containing the dependency's group and artifact id
	 * @return - true if it should be excluded (is contained), false otherwise 
	 */
	boolean contains(Identification identification);
	
	/**
	 * add a set of dependencies (via their identification parts) 
	 * @param identifications - the {@link Set} of {@link Identification} 
	 */
	void addAll( Set<Identification> identifications);
}
