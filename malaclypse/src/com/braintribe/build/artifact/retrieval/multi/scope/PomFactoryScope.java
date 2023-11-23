// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.scope;

import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomExpertFactory;

/**
 * a aggregation of pom related expert factories 
 * <ul>
 * 	<li> {@link PomExpertFactory} </li>
 * </ul>
 * @author pit
 *
 */
public interface PomFactoryScope {
	PomExpertFactory getPomExpertFactory();
	ArtifactPomReader getPomReader();	
}
