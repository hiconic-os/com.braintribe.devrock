// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.pom.properties;

import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.representations.artifact.pom.ArtifactPomReader;
import com.braintribe.build.artifact.virtualenvironment.VirtualPropertyResolver;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.maven.settings.Settings;
/**
 * interface that abstracts the property resolving features </br> 
 * implementing types are the {@link ArtifactPomReader} and the {@link MavenSettingsReader}
 * @author pit
 *
 */
public interface PomPropertyResolver extends VirtualPropertyResolver {
	/**
	 * resolve a value within the current {@link Model} or {@link Settings}
	 * @param expression - the variable to look for, without any ${...} etc, i.e. stripped 
	 * @return - the value as a string or null 
	 */
	String resolveValue( Artifact artifact, String expression);
	
	/**
	 * resolve a property (in most cases from the local property map)
	 * @param property - the key of the property to look up, without any ${..}, i.e. stripped
	 * @return - the property's value or null
	 */
	String resolveProperty( Artifact artifact, String property);
	
	/**
	 * the most complex function: parse a string for any variable reference and try to 
	 * resolve the expression, repeat until no variable's left.. the expression should contain ${..}, multiples (and recursive) are ok 
	 * @param expression - the string to parse
	 * @return - the string after value replacement took place or NULL if some variable couldn't be resolved
	 */
	String expandValue( String walkScopeId, Artifact artifact, String expression);

}
