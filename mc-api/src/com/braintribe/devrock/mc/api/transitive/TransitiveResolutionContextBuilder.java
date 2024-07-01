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
package com.braintribe.devrock.mc.api.transitive;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.braintribe.devrock.mc.api.download.PartEnrichingContext;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.essential.ArtifactIdentification;

/**
 * builder for the {@link TransitiveResolutionContext}
 * @author pit
 *
 */
public interface TransitiveResolutionContextBuilder {
	/**
	 * @param predicate - the filter to check if the artifact is to be traversed if passes 
	 * @return - itself
	 */
	TransitiveResolutionContextBuilder artifactTransitivityPredicate(Predicate<? super ArtifactPathElement> predicate);
	
	/**
	 * @param filter - the filter to select/discard an artifact based on its path up to the artifact
	 * @return 
	 */
	TransitiveResolutionContextBuilder artifactPathFilter(Predicate<? super ArtifactPathElement> filter);
	/**
	 * @param filter - the filter to select/discard a dependency based on its path up to the dependency
	 * @return - itself
	 */
	TransitiveResolutionContextBuilder dependencyPathFilter(Predicate<? super DependencyPathElement> filter);
	/**
	 * @param filter - the filter to select/discard an artifact based on the {@link AnalysisArtifact}
	 * @return - itself 
	 */
	TransitiveResolutionContextBuilder artifactFilter(Predicate<? super AnalysisArtifact> filter);
	/**
	 * @param filter - the filte to select/discard a dependency based on the {@link AnalysisDependency}
	 * @return - itself
	 */
	TransitiveResolutionContextBuilder dependencyFilter(Predicate<? super AnalysisDependency> filter);
	
	
	/**
	 * @param customScopeSupplier - returns a 'custom scope' for the given {@link AnalysisDependency}
	 * @return - itself
	 */
	TransitiveResolutionContextBuilder customScopeSuppplier(Function<? super AnalysisDependency, Object> customScopeSupplier);
	
	/**
	 * default parents are not included
	 * @param include - include parents in result if true, false otherwise
	 * @return - itself
	 */
	TransitiveResolutionContextBuilder includeParentDependencies(boolean include);
	
	/**
	 * default imports are not included
	 * @param include - include import-scoped dependencies in parent's depmgt section in result if true, false otherwise
	 * @return
	 */
	TransitiveResolutionContextBuilder includeImportDependencies(boolean include);
	
	/**
	 * default standard import are included 
	 * @param include - include standard dependencies in result if true, false otherwise
	 * @return
	 */
	TransitiveResolutionContextBuilder includeStandardDependencies(boolean include);
	
	/**
	 * default relocation dependencies are not included
	* @param include - include relocating dependencies in result if true, false otherwise
	 * @return
	 */
	TransitiveResolutionContextBuilder includeRelocationDependencies(boolean include);
	
	/**
	 * default exclusions are respected
	 * @param respect - true if exclusions should be respected, false otherwise
	 * @return
	 */
	TransitiveResolutionContextBuilder respectExclusions(boolean respect);
	
	/**
	 * @param partEnrichingContext
	 * @return
	 */
	TransitiveResolutionContextBuilder globalExclusions(Set<ArtifactIdentification> globalExclusions);

	/**
	 * @param partEnrichingContext - adds an {@link PartEnrichingContext}
	 * @return - itself
	 */
	TransitiveResolutionContextBuilder enrich(PartEnrichingContext partEnrichingContext);

	/**
	 * default no leniency
	 * @param include
	 * @return
	 */
	TransitiveResolutionContextBuilder lenient(boolean include);
	
	/**
	 * @param buildRange
	 * @return
	 */
	TransitiveResolutionContextBuilder buildRange(BuildRange buildRange);
	
	/**
	 * @return - a fully qualified {@link TransitiveResolutionContext} 
	 */
	TransitiveResolutionContext done();
}
