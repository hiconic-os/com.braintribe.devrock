// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.resolving;

import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;

import java.util.Collections;
import java.util.Set;

import com.braintribe.build.artifact.retrieval.multi.resolving.listener.DependencyResolverNotificationBroadcaster;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.artifact.version.Version;

/**
 * the functional interface for a dependency resolver
 * @author pit
 *
 */
public interface DependencyResolver extends DependencyResolverNotificationBroadcaster {


	Part resolvePom(String walkScopeId, Identification id, Version version) throws ResolvingException;
	Part resolvePomPart(String walkScopeId, Part pomPart) throws ResolvingException;
	
	/**
	 * deprecated, use @{@link #resolveTopDependency(String, Dependency)}..   
	 * resolves a dependency and returns max the two best matching solutions,
	 * one for release, one for snapshot. 
	 * @param walkScopeId - the id of the walk
	 * @param dependency - the {@link Dependency}
	 * @return - a {@link Set} with maximal two {@link Solution}
	 */
	@Deprecated
	default Set<Solution> resolveDependency(String walkScopeId, Dependency dependency) throws ResolvingException { return Collections.emptySet();}
	
	/**
	 * deprecated, use @{@link #resolveTopDependency(String, Dependency, String)}..
	 * @param redirectionUseCase - redirection use case, i.e. switching key
	 */
	@Deprecated
	default Set<Solution> resolveDependency(String walkScopeId, Dependency dependency, String redirectionUseCase) throws ResolvingException { 
		return resolveDependency(walkScopeId, dependency);	
	}
	
	default Solution resolveSingleTopDependency(String walkScopeId, Dependency dependency) throws ResolvingException {
		Set<Solution> solutions = resolveTopDependency(walkScopeId, dependency);
		
		if (isEmpty(solutions))
			return null;
		
		if(solutions.size() == 1)
			return solutions.iterator().next();

		return solutions.stream() // 
				.sorted(ArtifactProcessor.artifactComparator.reversed()) //
				.findFirst().get();
	}

	/**
	 * resolves a dependency and returns max the two best matching solutions,
	 * one for release, one for snapshot. 
	 * default implementation calls the deprecated {@link #resolveDependency(String, Dependency)}
	 * @param walkScopeId - the id of the walk
	 * @param dependency - the {@link Dependency}
	 * @return - a {@link Set} with maximal two {@link Solution}
	 */
	Set<Solution> resolveTopDependency(String walkScopeId, Dependency dependency) throws ResolvingException;

	/**
	 * resolves a dependency and returns max the two best matching solutions, while respecting the redirection use case.
	 * default is ignoring the use case
	 * @param walkScopeId - the id of the walk  
	 * @param dependency - the {@link Dependency}
	 * @param redirectionUseCase - the redirection use case switch
	 * @return - a {@link Set} with maximal two {@link Solution}
	 */
	default Set<Solution> resolveTopDependency(String walkScopeId, Dependency dependency, String redirectionUseCase) throws ResolvingException {
		return resolveTopDependency(walkScopeId, dependency);
	}
	
	
	default Solution resolve( String walkScopeId, Dependency dependency) throws ResolvingException { return null;}
	
	/**
	 * returns *all* solutions that match the dependency (in contrast to {@link #resolveTopDependency(String, Dependency)} 
	 * which only returns maximal one release and one snapshot {@link Solution} 
	 * @param walkScopeId - the id of the walk
	 * @param dependency - the {@link Dependency}
	 * @return - a {@link Set} with all {@link Solution} matching the dependency
	 */
	Set<Solution> resolveMatchingDependency(String walkScopeId, Dependency dependency) throws ResolvingException;
	
	
}
