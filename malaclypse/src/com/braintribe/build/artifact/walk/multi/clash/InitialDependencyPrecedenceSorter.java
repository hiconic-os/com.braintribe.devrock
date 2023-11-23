// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi.clash;

import java.util.Collection;
import java.util.List;

import com.braintribe.model.artifact.Dependency;

/**
 * an interface to hide the different sorting options, 
 * @author pit
 *
 */
public interface InitialDependencyPrecedenceSorter {
	List<Dependency> sortDependencies( Collection<Dependency> input);
}
