// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
