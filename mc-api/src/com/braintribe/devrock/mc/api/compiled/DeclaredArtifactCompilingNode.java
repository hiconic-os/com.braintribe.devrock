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