// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi;

import java.util.Collection;

import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.denotations.WalkDenotationType;

/**
 * @author pit
 *
 */
public interface Walker extends TerminalProvider {

	void acknowledeDenotationType(WalkDenotationType walkDenotationType);
	
	/**
	 * walk down the dependency tree, starting a the terminal, while clashing, 
	 * and eventually resolving all required parts of the resulting set of solutions
	 * @param walkScopeId - the id of this walk, e.g. a UUID's string value 
	 * @param solution - the terminal {@link Solution}, the starting point of the traversion
	 * @return - a {@link Collection} of {@link Solution} that make up the dependency tree
	 * @throws WalkException
	 */
	Collection<Solution> walk( String walkScopeId, Solution solution) throws WalkException;	
}
