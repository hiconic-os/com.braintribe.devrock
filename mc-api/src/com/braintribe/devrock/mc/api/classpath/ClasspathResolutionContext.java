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
package com.braintribe.devrock.mc.api.classpath;

import java.util.Set;
import java.util.function.Predicate;

import com.braintribe.devrock.mc.api.download.PartEnrichingContext;
import com.braintribe.devrock.mc.api.transitive.ArtifactPathElement;
import com.braintribe.devrock.mc.api.transitive.DependencyPathElement;
import com.braintribe.devrock.mc.impl.classpath.BasicClasspathResolutionContext;
import com.braintribe.model.artifact.analysis.ClashResolvingStrategy;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.analysis.AnalysisDependency;

/**
 * the configuration context for the {@link ClasspathDependencyResolver}
 * @author pit / dirk
 *
 */
public interface ClasspathResolutionContext {
	
	/**
	 * @return - whether it's lenient (default : false)
	 */
	boolean lenient();
	/**
	 * @return - the currently active {@link ClasspathResolutionScope}, aka the 'magic scope', 
	 * (default : compile)
	 */
	ClasspathResolutionScope scope();
	/**
	 * @return - the currently active strategy, either 'first visit' or 'highest version'
	 * (default : highest version)
	 */
	ClashResolvingStrategy clashResolvingStrategy();
	
	/**
	 * Enrich cp-relevant solutions with jar in a mandatory and logical way
	 * @return true if jars should be enriched (default : true)
	 */
	boolean enrichJar();
	
	/**
	 * Enrich cp-relevant solutions with -javadoc.jar in an optional way
	 * (default : false)
	 * @return - true if javadoc should be leniently enriched
	 */
	boolean enrichJavadoc();

	/**
	 * Enrich cp-relevant solutions with -sources.jar in an optional way
	 * (default : false)
	 * @return - true if sources should be leniently enriched
	 */
	boolean enrichSources();
	
	/**
	 * @return - the {@link PartEnrichingContext}
	 */
	PartEnrichingContext enrich();
	
	/**
	 * @return - the dependency filter (default: pass through, i.e. filter needs to 
	 * return true if the dependency is to be processed)
	 */
	Predicate<AnalysisDependency> dependencyFilter();
	
	/**
	 * @return - a {@link Set} of {@link ArtifactIdentification} that should be excluded throughout the tree
	 */
	Set<ArtifactIdentification> globalExclusions();
	
	/**
	 * @return - the artifact filter that sees the full path to the artifact (default : pass through). CURRENTLY NOT SUPPORTED IN ACTUAL RESOLUTION. 
	 */
	Predicate<? super ArtifactPathElement> artifactPathFilter();
	
	/**
	 * @return - the dependency filter that sees the full path to the dependency (default : pass through)
	 */
	Predicate<? super DependencyPathElement> dependencyPathFilter();

	
	/**
	 * the entry point for building a {@link ClasspathResolutionContext}
	 * @return
	 */
	static ClasspathResolutionContextBuilder build() {
		return new BasicClasspathResolutionContext();
	}
}
