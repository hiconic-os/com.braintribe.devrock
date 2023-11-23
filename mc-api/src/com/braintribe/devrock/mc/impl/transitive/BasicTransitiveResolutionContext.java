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
package com.braintribe.devrock.mc.impl.transitive;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.braintribe.devrock.mc.api.commons.Functions;
import com.braintribe.devrock.mc.api.download.PartEnrichingContext;
import com.braintribe.devrock.mc.api.transitive.ArtifactPathElement;
import com.braintribe.devrock.mc.api.transitive.BuildRange;
import com.braintribe.devrock.mc.api.transitive.DependencyPathElement;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContextBuilder;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.essential.ArtifactIdentification;

public class BasicTransitiveResolutionContext implements TransitiveResolutionContext, TransitiveResolutionContextBuilder {

	private Predicate<? super AnalysisArtifact>  artifactFilter = Functions.invariantTrue();
	private Predicate<? super AnalysisDependency> dependencyFilter = Functions.invariantTrue();
	private Function<? super AnalysisDependency, Object> customScopeSupplier = d -> null;
	private Predicate<? super DependencyPathElement> dependencyPathFilter = Functions.invariantTrue();
	private Predicate<? super ArtifactPathElement> artifactPathFilter = Functions.invariantTrue();
	private Predicate<? super ArtifactPathElement> artifactTransitivityPredicate = Functions.invariantTrue();
	private boolean includeParentDependencies = false;
	private boolean includeRelocationDependencies = false;
	private boolean includeImportDependencies = false;
	private boolean includeStandardDependencies = true;
	private boolean respectExclusions = true;
	private boolean lenient = false;
	private PartEnrichingContext partEnrichingContext;
	private BuildRange buildRange;
	private Set<ArtifactIdentification> globalExclusions;

	@Override
	public TransitiveResolutionContextBuilder artifactFilter(Predicate<? super AnalysisArtifact>  artifactFilter) {
		this.artifactFilter = artifactFilter;
		return this;
	}

	@Override
	public TransitiveResolutionContextBuilder dependencyFilter(Predicate<? super AnalysisDependency> dependencyFilter) {
		this.dependencyFilter = dependencyFilter;
		return this;
	}
	
	@Override
	public TransitiveResolutionContextBuilder artifactPathFilter(Predicate<? super ArtifactPathElement>  artifactPathFilter) {
		if (this.artifactPathFilter != null) {
			Predicate<? super ArtifactPathElement> pathFilter = this.artifactPathFilter;			
			this.artifactPathFilter = p -> pathFilter.test(p) && artifactPathFilter.test(p);
		}
		else {
			this.artifactPathFilter = artifactPathFilter;
		}
		return this;
	}
	
	@Override
	public TransitiveResolutionContextBuilder dependencyPathFilter(Predicate<? super DependencyPathElement> dependencyPathFilter) {
		if (this.dependencyPathFilter != null) {
			Predicate<? super DependencyPathElement> pathFilter = this.dependencyPathFilter;			
			this.dependencyPathFilter = p -> pathFilter.test( p) && dependencyPathFilter.test(p);
		}
		else {
			this.dependencyPathFilter = dependencyPathFilter;
		}
		return this;
	}
	
	@Override
	public TransitiveResolutionContextBuilder artifactTransitivityPredicate(
			Predicate<? super ArtifactPathElement> predicate) {
		this.artifactTransitivityPredicate = predicate;
		return this;
	}
	
	@Override
	public TransitiveResolutionContextBuilder customScopeSuppplier(
			Function<? super AnalysisDependency, Object> customScopeSupplier) {
		this.customScopeSupplier = customScopeSupplier;
		return this;
	}
	
	@Override
	public PartEnrichingContext enrich() {
		return partEnrichingContext;
	}

	@Override
	public TransitiveResolutionContextBuilder includeParentDependencies(boolean includeParentDependencies) {
		this.includeParentDependencies = includeParentDependencies;
		return this;
	}

	@Override
	public TransitiveResolutionContextBuilder includeImportDependencies(boolean includeImportDependencies) {
		this.includeImportDependencies = includeImportDependencies;
		return this;
	}

	@Override
	public TransitiveResolutionContextBuilder includeStandardDependencies(boolean includeStandardDependencies) {
		this.includeStandardDependencies = includeStandardDependencies;
		return this;
	}

	@Override
	public TransitiveResolutionContextBuilder includeRelocationDependencies(boolean includeRelocationDependencies) {
		this.includeRelocationDependencies = includeRelocationDependencies;
		return this;
	}

	@Override
	public TransitiveResolutionContextBuilder respectExclusions(boolean respectExclusions) {
		this.respectExclusions = respectExclusions;
		return this;
	}
	
	@Override
	public TransitiveResolutionContextBuilder globalExclusions(Set<ArtifactIdentification> globalExclusions) {
		this.globalExclusions = globalExclusions;
		return this;
	}

	@Override
	public TransitiveResolutionContextBuilder lenient(boolean lenient) {
		this.lenient = lenient;
		return this;
	}
	
	@Override
	public TransitiveResolutionContextBuilder enrich(PartEnrichingContext partEnrichingContext) {
		this.partEnrichingContext = partEnrichingContext;
		return this;
	}
	
	@Override
	public TransitiveResolutionContextBuilder buildRange(BuildRange buildRange) {
		this.buildRange = buildRange;
		return this;
	}
	
	@Override
	public TransitiveResolutionContext done() {
		return this;
	}

	@Override
	public Predicate<? super AnalysisArtifact>  artifactFilter() {
		return artifactFilter;
	}

	@Override
	public Predicate<? super AnalysisDependency> dependencyFilter() {
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
	public Predicate<? super ArtifactPathElement> artifactTransitivityPredicate() {
		return artifactTransitivityPredicate;
	}
	
	@Override
	public Function<? super AnalysisDependency, Object> customScopeSupplier() {
		return customScopeSupplier;
	}

	@Override
	public boolean includeParentDependencies() {
		return includeParentDependencies;
	}

	@Override
	public boolean includeImportDependencies() {
		return includeImportDependencies;
	}

	@Override
	public boolean includeStandardDependencies() {
		return includeStandardDependencies;
	}

	@Override
	public boolean includeRelocationDependencies() {
		return includeRelocationDependencies;
	}

	@Override
	public boolean respectExclusions() {
		return respectExclusions;
	}
	
	@Override
	public Set<ArtifactIdentification> globalExclusions() {
		return globalExclusions;
	}

	@Override
	public boolean lenient() {
		return lenient;
	}
	
	@Override
	public BuildRange buildRange() {
		return buildRange;
	}
}
