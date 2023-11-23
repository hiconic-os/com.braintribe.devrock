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
package com.braintribe.devrock.mc.api.compiled;

/**
 * a context for the pom compiler, i.e. the {@link DeclaredArtifactCompilingNode}</br>
 * if a value is false, any problem will lead to an Exception, if true, the compiled artifact
 * is flagged as invalid and has Reasons for it.
 * @author pit
 *
 */
public interface DeclaredArtifactCompilingNodeContext {
	
	/**
	 * @return - leniency for artifact instrumentation  
	 */
	boolean artifactLeniency();
	
	/**
	 * @return - leniency for dependency instrumentation 
	 */
	boolean dependencyLeniency();
	/**
	 * @return - leniency for the parent instrumentation 
	 */
	boolean parentLeniency();
	
	/**
	 * @return - leniency for the import statements within a parent
	 */
	boolean importLeniency();
	
}
