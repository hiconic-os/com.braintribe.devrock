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
package com.braintribe.devrock.mc.api.declared;

import java.io.File;
import java.util.function.Predicate;

import com.braintribe.model.artifact.declared.DeclaredArtifact;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.version.FuzzyVersion;
import com.braintribe.model.version.VersionRange;

/**
 * the builder for the {@link DeclaredGroupExtractionContext}
 * 
 * @author pit
 *
 */
public interface DeclaredGroupExtractionContextBuilder {

	/**
	 * @param location - the group's location as a string
	 * @return - itself, the builder
	 */
	DeclaredGroupExtractionContextBuilder location(String location);
	/**
	 * @param location - the group's location as a file 
	 * @return - itself, the builder
	 */
	DeclaredGroupExtractionContextBuilder location(File location);
	
	/**
	 * @param include - true if all read {@link DeclaredArtifact} should be added to the result
	 * @return - itself, the builder	
	 */
	DeclaredGroupExtractionContextBuilder includeMembers( boolean include);
	
	/**
	 * @param include - true if the parent {@link DeclaredArtifact} should be added to result
	 * @return - itself, the builder
	 */
	DeclaredGroupExtractionContextBuilder includeParent( boolean include);
	/**
	 * @param simplify - true if only the lower boundary of a {@link VersionRange} or {@link FuzzyVersion} should be returned  
	 * @return - itself, the builder
	 */
	DeclaredGroupExtractionContextBuilder simplifyRange( boolean simplify);
	
	/**
	 * @param sort - true if all 'sortable' output should be sorted alphabetically (members: {@link VersionedArtifactIdentification} standard, group-versions per alphabetically) 
	 * @return - itself, the builder
	 */
	DeclaredGroupExtractionContextBuilder sort( boolean sort);
	/**
	 * @param enforceRanges - true if a group access with a single version should be marked as a problem
	 * @return - itself, the builder
	 */
	DeclaredGroupExtractionContextBuilder enforceRanges( boolean enforceRanges);
	
	
	/**
	 * @param includeSelfreferences - true if references within the group (aka self-ref) are to be included in the output
	 * @return - itself, the builder
	 */
	DeclaredGroupExtractionContextBuilder includeSelfreferences( boolean includeSelfreferences);
	
	/**
	 * @param inclusionFilter -
	 * @return - itself, the builder
	 */
	DeclaredGroupExtractionContextBuilder inclusions( Predicate<String> inclusionFilter);
	/**
	 * @param regexp
	 * @return - itself, the builder
	 */
	DeclaredGroupExtractionContextBuilder inclusions( String regexp);
	
	/**
	 * @param exclusionFilter
	 * @return - itself, the builder
	 */
	DeclaredGroupExtractionContextBuilder exclusions( Predicate<String> exclusionFilter);
	/**
	 * @param regexp
	 * @return - itself, the builder
	 */
	DeclaredGroupExtractionContextBuilder exclusions( String regexp);
	/**
	 * @return - the constructed {@link DeclaredGroupExtractionContext} (itself as well)
	 */
	DeclaredGroupExtractionContext done();

}
