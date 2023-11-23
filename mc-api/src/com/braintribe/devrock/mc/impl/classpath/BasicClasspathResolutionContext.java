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
package com.braintribe.devrock.mc.impl.classpath;

import java.util.Set;
import java.util.function.Predicate;

import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContextBuilder;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionScope;
import com.braintribe.devrock.mc.api.commons.Functions;
import com.braintribe.devrock.mc.api.download.PartEnrichingContext;
import com.braintribe.devrock.mc.api.transitive.ArtifactPathElement;
import com.braintribe.devrock.mc.api.transitive.DependencyPathElement;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.analysis.ClashResolvingStrategy;
import com.braintribe.model.artifact.essential.ArtifactIdentification;

/**
 * basic implementation of the {@link ClasspathResolutionContext}, with an attached {@link ClasspathResolutionContextBuilder}
 * @author pit / dirk
 *
 */
public class BasicClasspathResolutionContext implements ClasspathResolutionContext, ClasspathResolutionContextBuilder {

	private boolean lenient = false;
	private ClasspathResolutionScope scope = ClasspathResolutionScope.compile;
	private ClashResolvingStrategy clashResolvingStrategy = ClashResolvingStrategy.highestVersion;
	private Predicate<AnalysisDependency> dependencyFilter = Functions.invariantTrue();
	private Predicate<? super DependencyPathElement> dependencyPathFilter = null;
	private Predicate<? super ArtifactPathElement> artifactPathFilter = null;
	private boolean enrichJar = true;
	private boolean enrichJavadoc = false;
	private boolean enrichSources = false;
	private PartEnrichingContext partEnrichingContext;
	private Set<ArtifactIdentification> globalExclusions;

	@Override
	public ClasspathResolutionContextBuilder lenient(boolean lenient) {
		this.lenient = lenient;
		return this;
	}
	
	@Override
	public ClasspathResolutionContext done() {
		return this;
	}
	
	@Override
	public boolean lenient() {
		return lenient;
	}
	
	@Override
	public ClasspathResolutionContextBuilder scope(ClasspathResolutionScope scope) {
		this.scope = scope;
		return this;
	}

	@Override
	public ClasspathResolutionContextBuilder enrichJar(boolean enrichJar) {
		this.enrichJar = enrichJar;
		return this;
	}
	
	@Override
	public ClasspathResolutionContextBuilder enrichJavadoc(boolean enrichJavadoc) {
		this.enrichJavadoc = enrichJavadoc;
		return this;
	}
	
	@Override
	public ClasspathResolutionContextBuilder enrichSources(boolean enrichSources) {
		this.enrichSources = enrichSources;
		return this;
	}
	
	@Override
	public ClasspathResolutionContextBuilder enrich(PartEnrichingContext partEnrichingContext) {
		this.partEnrichingContext = partEnrichingContext;
		return this;
	}
	
	@Override
	public ClasspathResolutionContextBuilder globalExclusions(Set<ArtifactIdentification> globalExclusions) {
		this.globalExclusions = globalExclusions;
		return this;
	}
	
	@Override
	public boolean enrichJar() {
		return this.enrichJar;
	}
	
	@Override
	public boolean enrichJavadoc() {
		return this.enrichJavadoc;
	}
	
	@Override
	public boolean enrichSources() {
		return this.enrichSources;
	}
	
	@Override
	public PartEnrichingContext enrich() {
		return partEnrichingContext;
	}
	
	@Override
	public ClasspathResolutionContextBuilder clashResolvingStrategy(ClashResolvingStrategy strategy) {
		this.clashResolvingStrategy = strategy;
		return this;
	}
	
	@Override
	public ClasspathResolutionContextBuilder filterDependencies(Predicate<AnalysisDependency> filter) {
		this.dependencyFilter = filter;
		return this;
	}
	
	@Override
	public ClasspathResolutionContextBuilder artifactPathFilter(Predicate<? super ArtifactPathElement>  artifactPathFilter) {
		this.artifactPathFilter = artifactPathFilter;
		return this;
	}
	
	@Override
	public ClasspathResolutionContextBuilder dependencyPathFilter(Predicate<? super DependencyPathElement> dependencyPathFilter) {
		this.dependencyPathFilter = dependencyPathFilter;
		return this;
	}

	@Override
	public ClasspathResolutionScope scope() {
		return scope;
	}
	
	@Override
	public ClashResolvingStrategy clashResolvingStrategy() {
		return clashResolvingStrategy;
	}
	
	@Override
	public Predicate<AnalysisDependency> dependencyFilter() {
		return dependencyFilter;
	}

	@Override
	public Predicate<? super ArtifactPathElement> artifactPathFilter() {
		return artifactPathFilter;
	}

	@Override
	public Predicate<? super DependencyPathElement> dependencyPathFilter() {
		return dependencyPathFilter;
	}
	
	@Override
	public Set<ArtifactIdentification> globalExclusions() {
		return globalExclusions;
	}
}
