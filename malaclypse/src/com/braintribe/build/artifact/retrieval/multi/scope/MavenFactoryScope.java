// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.retrieval.multi.scope;

import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsExpertFactory;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;

/**
 * a scope that gives access to the <br/>
 * <ul>
 * 	<li> {@link MavenSettingsExpertFactory} </li>
 *  <li> {@link MavenSettingsReader} </li>
 * </ul>
 * @author pit
 *
 */
public interface MavenFactoryScope {
	/**
	 * get the expert factory that finds settings.xml 
	 * @return - return a {@link MavenSettingsExpertFactory}
	 */
	MavenSettingsExpertFactory getMavenExpertFactory();
	/**
	 * create and return a reader for the settings combination 
	 * @return - a {@link MavenSettingsReader} instance
	 */
	MavenSettingsReader getMavenSettingsReader();
}
