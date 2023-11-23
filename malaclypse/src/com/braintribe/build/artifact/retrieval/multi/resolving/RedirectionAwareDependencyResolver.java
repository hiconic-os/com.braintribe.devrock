// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.resolving;

import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomExpertFactory;

public interface RedirectionAwareDependencyResolver {
	/**
	 * if set, the resolver can retrieve a {@link ArtifactPomReader} to determine whether a redirection 
	 * is in the pom, and automatically return the correct pom 
	 * deprecated : see {@link RedirectionAwareDependencyResolver.#setPomReader(ArtifactPomReader)}
	 * @param factory - a {@link PomExpertFactory}
	 */
	@Deprecated
	void setPomExpertFactory(PomExpertFactory factory);
	
	/**
	 * if set, the resolver 
	 * @param reader
	 */
	default void setPomReader( ArtifactPomReader reader) {}
	
}
