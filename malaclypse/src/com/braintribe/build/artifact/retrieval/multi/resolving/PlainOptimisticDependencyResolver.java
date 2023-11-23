// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.resolving;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.braintribe.build.artifact.api.DependencyResolver;
import com.braintribe.build.artifact.retrieval.multi.enriching.MultiRepositorySolutionEnricher;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.version.VersionProcessor;

/**
 * a DependencyResolver that returns strictly one solution per entered dependency.<br/>
 * the set it returns is a {@link LinkedHashSet}, and at each place the solution of the dependency at the same index
 * in the input is put. If no matching solution is found, that place in the set is set to null,
 * the {@link PlainOptimisticDependencyResolver} automatically selects the solution with the highest version
 * that matches the dependencies range. 
 * 
 * @author pit
 *
 */
public class PlainOptimisticDependencyResolver implements DependencyResolver {
	private com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver delegate;
	private MultiRepositorySolutionEnricher enricher;


	@Configurable @Required
	public void setDelegate(com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver delegate) {
		this.delegate = delegate;
	}
	
	@Configurable @Required
	public void setEnricher(MultiRepositorySolutionEnricher enricher) {
		this.enricher = enricher;
	}
		
	@Override
	public Set<Solution> resolve(Iterable<Dependency> dependencies) {
		String walkScopeId = UUID.randomUUID().toString();
		Set<Solution> result = new LinkedHashSet<>();
		for (Dependency dependency : dependencies) {
			Set<Solution> resolvedRawSolutions = delegate.resolveDependency( walkScopeId, dependency);
			if (resolvedRawSolutions == null || resolvedRawSolutions.size() == 0) {
				resolvedRawSolutions.add( null);
				continue;
			}
			Solution solution = pickHighestSolution( resolvedRawSolutions);
			result.add( solution);
		}
		enricher.enrich( walkScopeId, result); 
		return result;
	}

	/**
	 * sort the solutions so that the highest version is <br>lowest</br> in list,
	 * and the take the first entry 
	 * @param solutions - the {@link Set} with the solutions
	 * @return - the {@link Solution} with the highest version 
	 */
	private Solution pickHighestSolution(Set<Solution> solutions) {
		List<Solution> sorted = new ArrayList<>( solutions);
		sorted.sort( new Comparator<Solution>() {
			@Override
			public int compare(Solution o1, Solution o2) {					
				if (VersionProcessor.isHigher( o1.getVersion(), o2.getVersion()))
					return -1;
				if (VersionProcessor.isLess( o1.getVersion(), o2.getVersion()))
					return 1;
				return 0;
			}
	
		});
		return sorted.get(0);
	}

}
