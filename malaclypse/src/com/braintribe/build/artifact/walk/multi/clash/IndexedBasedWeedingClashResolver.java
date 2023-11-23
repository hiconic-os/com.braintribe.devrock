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
import com.braintribe.model.malaclypse.cfg.denotations.clash.ResolvingInstant;

/**
 * an implementation of the {@link AbstractWeedingClashResolver} that decides on first served, first use (as Maven does)<br/>
 * has a feature to switch between posthoc (default) and adhoc resolving instance via {@link #setResolvingInstant(ResolvingInstant)}<br/>
 * if set to adhoc, it will retain any dependency by its identification, and never accept any other with the same group and artifact.<br/<
 * @author Pit
 *
 */
public class IndexedBasedWeedingClashResolver extends AbstractWeedingClashResolver {
	@Override
	protected void sort(List<Dependency> clashes) {
		// sort : descending !! (viewer expects winner to be at the bottom of the list)			
		Collections.sort( clashes, new Comparator<Dependency>() {

			@Override
			public int compare(Dependency o1, Dependency o2) {					
				return o1.getPathIndex().compareTo( o2.getPathIndex())*-1;
			}
		});	
		
	}

}
