// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi.clash;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.braintribe.model.artifact.Dependency;

/**
 * sort dependency by their place in the hierarchy : <br/>
 * any dependencies of a pom get the same index
 * @author pit
 *
 */
public class InitialDependencySortByHierarchy implements InitialDependencyPrecedenceSorter {

	@Override
	public List<Dependency> sortDependencies(Collection<Dependency> input) {
		List<Dependency> result = new ArrayList<Dependency>( input);
		Collections.sort( result, new Comparator<Dependency>() {

			@Override
			public int compare(Dependency o1, Dependency o2) {
				return o1.getHierarchyLevel().compareTo(o2.getHierarchyLevel()); // lower hierarchy level means higher up in relevance 
			}
		
		});
		return result;
	}

}
