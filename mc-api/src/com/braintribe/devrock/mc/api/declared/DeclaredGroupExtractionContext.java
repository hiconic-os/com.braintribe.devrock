// ============================================================================
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
package com.braintribe.devrock.mc.api.declared;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.impl.declared.BasicDeclaredGroupExtractionContext;
import com.braintribe.model.artifact.declared.DeclaredArtifact;
import com.braintribe.model.version.FuzzyVersion;
import com.braintribe.model.version.VersionRange;

/**
 * a context to parameterize the group extractor 
 *   
 * @author pit
 *
 */
public interface DeclaredGroupExtractionContext {
	
	/**
	 * @return - the path to the group's directory 
	 */
	String getGroupLocation();
	/**
	 * @return - true if all {@link DeclaredArtifact} of the group should be returned.
	 */
	boolean includeMembers();
	/**
	 * @return - true if the parent should be returned 
	 */
	boolean includeParent();
	
	/**
	 * @return - changes the returned ranges to be only represented by their lower boundary (careful, inclusive/exclusive info gets lost then) 
	 */
	boolean simplifyRangeToLowerBoundary();
	/**
	 * @return - true if members and group versions should be sorted 
	 */
	boolean sort();
	/**
	 * @return - true if access to groups (after filtering) do not contain a {@link VersionRange} or a {@link FuzzyVersion}
	 */
	boolean enforceRanges();

	/**
	 * @return - the exclusion filter 
	 */
	Predicate<String> exclusionFilter();
	/**
	 * @return -  the inclusion filter
	 */
	Predicate<String> inclusionFilter();
	
	/**
	 * @return - true if references within the group should be included in the ouput
	 */
	boolean includeSelfreferences();
	

	/* helpers for tracking artifacts that still have dependencies that require a dep mgt section in the parent*/
	List<Pair<String, Map<String, String>>> getManagementDependentDependencies();
	void setManagementDependentDependencies( List<Pair<String, Map<String, String>>> deps);

	
	static DeclaredGroupExtractionContextBuilder build() {
		return new BasicDeclaredGroupExtractionContext();
	}
}
