// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.maven.settings.properties;

import com.braintribe.build.artifact.virtualenvironment.VirtualPropertyResolver;

/**
 * interface for property resolving properties (remnant of the Maven pom parsing) (
 * @author pit
 *
 */
public interface SettingsPropertyResolver extends VirtualPropertyResolver {

	String resolveProperty(String property);
	String resolveValue(String expression);
	String expandValue(String line);
}
