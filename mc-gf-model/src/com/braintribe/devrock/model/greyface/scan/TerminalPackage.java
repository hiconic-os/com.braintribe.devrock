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
package com.braintribe.devrock.model.greyface.scan;

import java.util.List;

import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * the result as scanned for a dependency
 * @author pit
 *
 */
public interface TerminalPackage extends CompiledDependencyIdentification {
	
	EntityType<TerminalPackage> T = EntityTypes.T(TerminalPackage.class);

	/**
	 * @return - a {@link List} of {@link RepoPackage} as returned by the scanner 
	 */
	List<RepoPackage> getScanResult();
	void setScanResult(List<RepoPackage> scanResult);

	/**
	 * @return - the dependencies as {@link TerminalPackage}s
	 */
	List<TerminalPackage> getDependencies();
	void setDependencies(List<TerminalPackage> value);

	
}
