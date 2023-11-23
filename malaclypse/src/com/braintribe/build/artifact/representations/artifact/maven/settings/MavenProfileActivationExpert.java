// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.maven.settings;

import com.braintribe.build.artifact.representations.artifact.maven.settings.properties.SettingsPropertyResolver;
import com.braintribe.model.maven.settings.Activation;
import com.braintribe.model.maven.settings.Profile;

/**
 * an expert that can decided whether the passed profile is to be considered active or not<br/> 
 * standard implementation {@link MavenProfileActivationExpertImpl} follows the maven logic,
 * other implementations can decide on other logics.  
 * 
 * @author pit
 *
 */
public interface MavenProfileActivationExpert {

	/**
	 * grants access to property resolving across the settings.xml - via the {@link MavenSettingsReader}
	 * @param propertyResolver - the currently active {@link SettingsPropertyResolver}
	 */
	void setPropertyResolver(SettingsPropertyResolver propertyResolver);

	/**
	 * determines if the profile is active 
	 * @param profile - the full {@link Profile} (to access the id for instance)
	 * @param activation - the {@link Activation} subpart of the profile (as maven does it)
	 * @return true if active, false if inactive, null if it won't decide (for custom implementations)
	 */
	Boolean isActive(Profile profile, Activation activation);

}
