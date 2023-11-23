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
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.VersionRange;

/**
 * an implementation of the {@link AbstractWeedingClashResolver} that uses an optimistic resolving approach: the higher versions wins
 * @author Pit
 *
 */
public class OptimisticWeedingClashResolver extends AbstractWeedingClashResolver {
	/**
	 * sort a {@link List} of {@link Dependency} according the {@link VersionRange}, via the {@link VersionRangeProcessor}
	 * @param clashes - the {@link List} of {@link Dependency} to sort
	 */
	@Override
	protected void sort( List<Dependency> clashes) {
		Collections.sort( clashes, new Comparator<Dependency>() {
			@Override
			public int compare(Dependency o1, Dependency o2) {					
				VersionRange range1 = o1.getVersionRange();
				VersionRange range2 = o2.getVersionRange();
				// only test in one way : higher wins, all others are less
				return VersionRangeProcessor.compare(range1, range2);								
			}
			
		});
	}
}
