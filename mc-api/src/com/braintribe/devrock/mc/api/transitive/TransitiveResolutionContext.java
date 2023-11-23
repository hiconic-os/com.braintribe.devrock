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
import com.braintribe.devrock.mc.impl.transitive.BasicTransitiveResolutionContext;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.essential.ArtifactIdentification;

/**
 * configuration context for the transitive resolver 
 * @author pit / dirk
 *
 */
public interface TransitiveResolutionContext {
	/**
	 * @return - the {@link Predicate} to influence what artifacts are transitively traversed 
	 */
	Predicate<? super ArtifactPathElement> artifactTransitivityPredicate();
	/**
	 * @return
	 */
	Predicate<? super ArtifactPathElement> artifactPathFilter();
	/**
	 * @return
	 */
	Predicate<? super DependencyPathElement> dependencyPathFilter();
	
	/**
	 * @return - must return true if an artifact is to be considered 
	 */
	Predicate<? super AnalysisArtifact> artifactFilter();
	
	/**
	 * @return - must return true if a dependency is to considered
	 */
	Predicate<? super AnalysisDependency> dependencyFilter();
	
	
	/**
	 * @return
	 */
	Function<? super AnalysisDependency, Object> customScopeSupplier();
	
	/**
	 * @return - true if parents should appear in the result 
	 */
	boolean includeParentDependencies();
	/**
	 * @return - true if the imports from within parents should appear in the result 
	 */
	boolean includeImportDependencies();
	/**
	 * @return - true if standard (plain vanilla) dependencies should appear in the result 
	 */
	boolean includeStandardDependencies();
	/**
	 * @return - true if relocating artifacts should appear in the result, 
	 * additionally to the artifact it redirected to
	 */
	boolean includeRelocationDependencies();
	/**
	 * @return - true if exclusions (attached to a dependency) should be respected
	 */
	boolean respectExclusions();
	/**
	 * @return - false if any problems lead to an immediate exception,
	 * otherwise any problematic values are flagged  
	 */
	boolean lenient();
	
	/**
	 * @return - a {@link PartEnrichingContext} with information about what to enrich of the passed {@link AnalysisArtifact}
	 */
	PartEnrichingContext enrich();
	
	/**
	 * @return - the {@link BuildRange} to be used - if any. 
	 */
	BuildRange buildRange();
	
	/**
	 * @return - the {@link Set} of {@link ArtifactIdentification} to act as exclusions throught the tree
	 */
	Set<ArtifactIdentification> globalExclusions();

	/**
	 * @return - a {@link TransitiveResolutionContextBuilder} to start building
	 */
	static TransitiveResolutionContextBuilder build() {
		return new BasicTransitiveResolutionContext();
	}
}

