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
package com.braintribe.devrock.mc.api.classpath;

import java.util.Set;
import java.util.function.Predicate;

import com.braintribe.devrock.mc.api.download.PartEnrichingContext;
import com.braintribe.devrock.mc.api.transitive.ArtifactPathElement;
import com.braintribe.devrock.mc.api.transitive.DependencyPathElement;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.analysis.ClashResolvingStrategy;
import com.braintribe.model.artifact.essential.ArtifactIdentification;

/**
 * interface of the builder that can create {@link ClasspathResolutionContext}
 * @author pit / dirk
 *
 */
public interface ClasspathResolutionContextBuilder {
	/**
	 * @param scope - the {@link ClasspathResolutionScope}, aka 'magic scope'
	 * @return - itself
	 */
	ClasspathResolutionContextBuilder scope(ClasspathResolutionScope scope);
	
	/**
	 * @param filter - the dependency filter to use
	 * @return - itself
	 */
	ClasspathResolutionContextBuilder filterDependencies(Predicate<AnalysisDependency> filter);
	
	/**
	 * @param globalExclusions - the {@link Set} of {@link ArtifactIdentification} to exclude from the tree
	 * @return - itself
	 */
	ClasspathResolutionContextBuilder globalExclusions(Set<ArtifactIdentification> globalExclusions);
	
	/**
	 * @param filter - the filter to select/discard an artifact based on its path up to the artifact
	 * @return 
	 */
	ClasspathResolutionContextBuilder artifactPathFilter(Predicate<? super ArtifactPathElement> filter);
	/**
	 * @param filter - the filter to select/discard a dependency based on its path up to the dependency
	 * @return - itself
	 */
	ClasspathResolutionContextBuilder dependencyPathFilter(Predicate<? super DependencyPathElement> filter);
	
	/**
	 * @param strategy - the {@link ClashResolvingStrategy} to resolve clashes
	 * @return - itself
	 */
	ClasspathResolutionContextBuilder clashResolvingStrategy(ClashResolvingStrategy strategy);
	
	/**
	 * @param enrichJar - whether to enrich jars. If active and required jars are missing,
	 * it will generate an error (failed resolution, reason) 
	 * @return - itself
	 */
	ClasspathResolutionContextBuilder enrichJar(boolean enrichJar);
	
	/**
	 * @param enrichJavadoc - whether to enrich javadoc. Is lenient, i.e. missing javadoc are ok
	 * @return
	 */
	ClasspathResolutionContextBuilder enrichJavadoc(boolean enrichJavadoc);
	/**
	 * @param enrichSources - whether to enrich sources. Is lenient, i.e. missing sources are ok
	 * @return - itself
	 */
	ClasspathResolutionContextBuilder enrichSources(boolean enrichSources);
	
	/**
	 * default no leniency
	 * @param lenient - whether to be lenient (if not, it will abort on issues, otherwise return a resolution that is 
	 * potentially flagged as 'failed' 
	 * @return - itself 
	 */
	ClasspathResolutionContextBuilder lenient(boolean lenient);
	
	/**
	 * @return - an instance of the configured {@link ClasspathResolutionContext}
	 */
	ClasspathResolutionContext done();
	
	/**
	 * @param partEnrichingContext
	 * @return
	 */
	ClasspathResolutionContextBuilder enrich(PartEnrichingContext partEnrichingContext);
}
