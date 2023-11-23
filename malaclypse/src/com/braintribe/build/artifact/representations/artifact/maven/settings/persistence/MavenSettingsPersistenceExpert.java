// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.maven.settings.persistence;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.model.maven.settings.Settings;

/**
 * an API for the persistence expert of the maven settings, 
 * i.e. the class that loads the two settings.xml and merges their settings
 * <br/>
 * 
 * @author pit
 *
 */
public interface MavenSettingsPersistenceExpert {

	public static final String MC_ORIGIN = "mc_origin";

	/**
	 * loads (and merges) the contents of the two settings.xml 
	 * @return - a {@link Settings} instance 
	 * @throws RepresentationException - arrgh
	 */
	Settings loadSettings() throws RepresentationException;

}
