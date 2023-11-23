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
package com.braintribe.devrock.mc.api.compiled;

import java.util.List;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.declared.DeclaredArtifact;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

/**
 * the interface for the pom compiler complex - represents a pom, and compiles it 
 * @author pit / dirk
 *
 */
public interface DeclaredArtifactCompilingNode {

	/**
	 * resolves a variable, via env-, pom-internal-, pom-, system-properties plus delegating it to parent chain if required 
	 * @param variable - the variable (only name, without ${..})
	 */
	
	Maybe<String> getPropertyReasoned(String name);
	
//	/**
//	 * @return - a Stream of a {@link Pair} of the {@link DeclaredDependency} and its declared {@link VersionExpression}
//	 */
//	Stream<Pair<DeclaredDependency, VersionExpression>> getEffectiveMangagedDependencies(CompiledArtifactResolutionContext context);
	
	/**
	 * @return - the {@link VersionedArtifactIdentification} that identifies this node
	 */
	VersionedArtifactIdentification getIdentification();
	
	
	/**
	 * @param a resolution context that is null at the entry point
	 * @return the {@link DeclaredArtifact} that holds all merged information of this node and of all transitive parents/imports
	 */
	DeclaredArtifact getAggregatedDeclaredArtifact(CompiledArtifactResolutionContext context);
	
	/**
	 * @param a resolution context that is null at the entry point
	 * @return the {@link AggregatedDeclaredArtifactNode} that holds all merged information of this node and of all transitive parents/imports
	 */
	AggregatedDeclaredArtifactNode getAggregatedDeclaredArtifactNode(CompiledArtifactResolutionContext context);
	
	
	/**
	 * @param a resolution context that is null at the entry point
	 * @return the {@link DeclaredArtifact} that holds all merged and resolved information of this node and of all transitive parents/imports
	 */
	DeclaredArtifact getEffectiveDeclaredArtifact(CompiledArtifactResolutionContext context); 
	
	
	/**
	 * @return - a {@link CompiledArtifact} compiled from this node - fully qualified and resolved,
	 * otherwise flagged 
	 */
	CompiledArtifact getCompiledArtifact(CompiledArtifactResolutionContext context);
	

	/**
	 * @return -the {@link DeclaredArtifact} that is currently being compiled
	 */ 
	DeclaredArtifact getRawArtifact();
	
	/**
	 * @return - true if any reasons for the invalidity exist
	 */
	default boolean valid() {
		return invalidationReasons() != null;
	}
	
	/**
	 * @return a {@link List} of {@link Reason} that lead to the node being invalid, collected during compilation  
	 */
	List<Reason> invalidationReasons();
	
}