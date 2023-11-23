// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.walk.multi.clash;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.braintribe.model.artifact.Dependency;

/**
 * an implementation of the {@link AbstractWeedingClashResolver} that decides on the depth, i.e. the hierarchical level 
 * @author Pit
 *
 */
public class DepthBasedWeedingClashResolver extends AbstractWeedingClashResolver {

	@Override
	protected void sort(List<Dependency> clashes) {
		// sort : descending !! (viewer expects winner to be at the bottom of the list)			
		Collections.sort( clashes, new Comparator<Dependency>() {

			@Override
			public int compare(Dependency o1, Dependency o2) {
				int result = o1.getHierarchyLevel().compareTo( o2.getHierarchyLevel());
				if (result != 0) {
					return result*-1;
				} 
				else {
					return o1.getPathIndex().compareTo( o2.getPathIndex())*-1;
				}
			}
		});	
	}

	

}
