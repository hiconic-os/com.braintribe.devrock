// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.artifacts.test.maven.pom.marshall.validator;

import java.util.List;
import java.util.Map;

import com.braintribe.model.artifact.Dependency;

/**
 * context for validation .. 
 * @author pit
 *
 */
public interface ValidatorContext {
	/**
	 * @return - the {@link Dependency} to test
	 */
	Dependency dependency();
	
	/**
	 * @return - expected groupId
	 */
	String groupId();
	/**
	 * @return - expected artifactId
	 */
	String artifactId();
	/**
	 * @return - expected version
	 */
	String version();
	
	/**
	 * @return - expected scope
	 */
	String scope();
	/**
	 * @return
	 */
	String type();
	/**
	 * @return
	 */
	Boolean optional();
	
	/**
	 * @return
	 */
	String group();	
	/**
	 * @return
	 */
	List<String> tags();
	Map<String,String> redirects();
	Map<String,String> virtualParts();
	
}
