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
package com.braintribe.devrock.mc.core.compiled;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.devrock.mc.api.compiled.CompiledArtifactDependencyKind;
import com.braintribe.devrock.mc.api.compiled.CompiledArtifactResolutionContext;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

/**
 * @author pit / dirk
 *
 */
public class BasicCompiledArtifactResolutionContext implements CompiledArtifactResolutionContext {
	
	private CompiledArtifactResolutionContext depender;
	private CompiledArtifactDependencyKind kind;
	private VersionedArtifactIdentification artifactIdentification;
	private List<Reason> invalidationReasons = new ArrayList<>();
	
	/**
	 * creates a new {@link BasicCompiledArtifactResolutionContext}
	 * @param artifactIdentification - the {@link VersionedArtifactIdentification} of the pom
	 * @param depender - the ancestor {@link BasicCompiledArtifactResolutionContext}
	 * @param kind - the type of the pom, one of {@link CompiledArtifactDependencyKind}'s values
	 */
	public BasicCompiledArtifactResolutionContext(VersionedArtifactIdentification artifactIdentification, CompiledArtifactResolutionContext depender, CompiledArtifactDependencyKind kind) {
		this.artifactIdentification = artifactIdentification;
		this.depender = depender;
		this.kind = kind;
		
		checkCyclic();
	}
	
	/**
	 * creates a new 'default' {@link BasicCompiledArtifactResolutionContext}
	 * @param artifactIdentification - the {@link VersionedArtifactIdentification} of the pom
	 */
	public BasicCompiledArtifactResolutionContext(VersionedArtifactIdentification artifactIdentification) {
		this.artifactIdentification = artifactIdentification;
		this.kind = CompiledArtifactDependencyKind.STANDARD;
	}
	
	@Override
	public CompiledArtifactDependencyKind kind() {
		return kind;
	}

	@Override
	public CompiledArtifactResolutionContext depender() {
		return depender;
	}
	
	@Override
	public VersionedArtifactIdentification artifactIdentification() {
		return artifactIdentification;
	}
	
	/**
	 * @return - true if the chain of {@link BasicCompiledArtifactResolutionContext} is cyclic, i.e. one entry appears twice
	 */
	private boolean isCyclic() {
		CompiledArtifactResolutionContext context = depender();
		
		while (context != null) {
			if (HashComparators.versionedArtifactIdentification.compare(artifactIdentification, context.artifactIdentification()))
				return true;
			
			context = context.depender();
		}
		
		return false;
	}
	
	/**
	 * check for a cycle and throw an {@link Exception} if it finds one
	 */
	private void checkCyclic() {
		if (isCyclic()) {
			throw new IllegalStateException(getCycleErrorMessage());
		}
	}
	
	/**
	 * construct an error message showing the cycle
	 * @return - a formatted string with error message and path to cycle
	 */
	private String getCycleErrorMessage() {
		CompiledArtifactResolutionContext context = depender();
		StringBuilder builder = new StringBuilder();
		
		builder.append("Invalid parent structure due to cycle in dependency path\n\n");
		
		builder.append(asIdentificationString());
		builder.append("\n");
		
		while (context != null) {
			builder.append("  ");
			builder.append(context.asIdentificationString());
			builder.append("\n");
			
			if (HashComparators.versionedArtifactIdentification.compare(artifactIdentification, context.artifactIdentification())) {
				return builder.toString();
			}
			
			context = context.depender();
		}
		
		throw new IllegalStateException("Unexpected code flow when building cycle error message");
	}
	
	/**
	 *@return - a concatenation of {@link CompiledArtifactDependencyKind}'s value and the {@link VersionedArtifactIdentification}
	 */
	public String asIdentificationString() {
		return kind.name().toLowerCase() + " " + artifactIdentification.asString();
	}

	@Override
	public List<Reason> invalidationReasons() {
		return invalidationReasons;
	}
	
	
	
}
