// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.maven.settings;

import com.braintribe.build.artifact.representations.RepresentationException;

/**
 * interface for experts that override the local repository as defined in the settings.xml
 * can be used by AC for instance, seet {@link MavenSettingsExpertFactory} to set it <br/>
 * 
 * @author Pit
 *
 */
public interface LocalRepositoryLocationProvider {

	String getLocalRepository( String expression) throws RepresentationException;
	default String getLocalRepository() { return getLocalRepository(null); }
}
