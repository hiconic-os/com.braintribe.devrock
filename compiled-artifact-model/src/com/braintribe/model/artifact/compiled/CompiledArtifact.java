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
package com.braintribe.model.artifact.compiled;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.model.artifact.declared.DeclaredArtifact;
import com.braintribe.model.artifact.declared.Relocation;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * a fully qualified artifact (as opposed to the {@link DeclaredArtifact} it ready for processing)
 * @author pit
 *
 */
public interface CompiledArtifact extends CompiledArtifactIdentification, CompiledTerminal {
	
	EntityType<CompiledArtifact> T = EntityTypes.T(CompiledArtifact.class);

	String properties = "properties";
	String packaging = "packaging";
	String origin = "origin";
	String preEvaluatedArtifact = "preEvaluatedArtifact";
	String archetype = "archetype";
	String dependencies = "dependencies";
	String managedDependencies = "managedDependencies";
	String dependencyManagementFailure = "dependencyManagementFailure";
	String artifactRedirects = "artifactRedirects";
	String exclusions = "exclusions";
	String dominants = "dominants";
	String parent = "parent";
	String imports = "imports";
	String invalid = "invalid";
	String whyInvalid = "whyInvalid";
	String propertyProblems = "propertyProblems";
	String importSolutions = "importSolutions";
	String parentSolution = "parentSolution";
	
	DeclaredArtifact getOrigin();
	void setOrigin(DeclaredArtifact origin);
	
	/**
	 * A clone of the origin where all property placeholders beginning with "project." or "parent." are evaluated 
	 */
	DeclaredArtifact getPreEvaluatedArtifact();
	void setPreEvaluatedArtifact(DeclaredArtifact preEvaluatedArtifact);
	
	/**
	 * @return - the packaging of the artifact
	 */
	String getPackaging();
	void setPackaging( String packaging);
	
	/**	 
	 * @return - a map of the 'resolved' properties
	 */
	Map<String,String> getProperties();
	void setProperties( Map<String,String> properties);

	
	/**
	 * actually a property, but still : more prominent
	 * @return - the archetype property 
	 */
	String getArchetype();
	void setArchetype(String value);
	
	/**
	 * A map of reasons associated to property names if a property resolution failed
	 */
	Map<String,Reason> getPropertyProblems();
	void setPropertyProblems(Map<String,Reason> propertyProblems);
	
	/**
	 * @return - the {@link Map} of {@link CompiledDependencyIdentification} that declare artifact redirections, that transitively apply to the artifact's dependency tree 
	 */
	Map<CompiledDependencyIdentification,CompiledDependencyIdentification> getArtifactRedirects();
	void setArtifactRedirects(Map<CompiledDependencyIdentification,CompiledDependencyIdentification>  ArtifactRedirects);
	
	/**
	 * @return - a set of {@link ArtifactIdentification} to denote transitive exclusions
	 */
	Set<ArtifactIdentification> getExclusions();
	void setExclusions(Set<ArtifactIdentification> exclusions);
	
	/**
	 * @return - a list of {@link CompiledDependencyIdentification} that will (transitively) win in all subsequent clash resolving
	 */
	List<CompiledDependencyIdentification> getDominants();
	void setDominants(List<CompiledDependencyIdentification> dominants);	
	
	/**
	 * @return - the list of fully qualified dependencies
	 */
	List<CompiledDependency> getDependencies();
	void setDependencies( List<CompiledDependency> dependencies);
	
	List<CompiledDependency> getManagedDependencies();
	void setManagedDependencies( List<CompiledDependency> Managed);
	
	Reason getDependencyManagementFailure();
	void setDependencyManagementFailure(Reason dependencyManagementFailure);
	
	/**
	 * @return - {@link Relocation}
	 */
	Relocation getRelocation();
	void setRelocation(Relocation value);
	
	/**
	 * @return - all imports within its dependency management section
	 */
	List<CompiledDependencyIdentification> getImports();
	void setImports(List<CompiledDependencyIdentification> imports);
	
	List<ImportSolution> getImportSolutions();
	void setImportSolutions(List<ImportSolution> importSolutions);
	
	/**
	 * @return - the parent
	 */
	CompiledDependencyIdentification getParent();
	void setParent(CompiledDependencyIdentification parent);
	
	CompiledSolution getParentSolution();
	void setParentSolution(CompiledSolution parentSolution);
	
	/**
	 * Marks the artifact as invalid to allow lenient processing. If true you may find a reason for the invalidity with {@link #getWhyInvalid()}
	 */
	boolean getInvalid();
	void setInvalid(boolean invalid);
	
	/**
	 * holds a {@link Reason} for invalidity in case of {@link #getInvalid()} is true. 
	 */
	Reason getWhyInvalid();
	void setWhyInvalid(Reason reason);

	default Maybe<String> findProperty(String name) {
		String value = getProperties().get(name);
		
		if (value != null)
			return Maybe.complete(value);
		
		Reason reason = getPropertyProblems().get(name);
		
		if (reason != null)
			return reason.asMaybe();
		
		return Maybe.complete(null);
	}
	
	default Maybe<String> requireProperty(String name) {
		Maybe<String> valueMaybe = findProperty(name);
		
		if (valueMaybe.isUnsatisfied())
			return valueMaybe;
		
		
		String value = valueMaybe.get();
		
		if (value != null)
			return valueMaybe;
		
		return Reasons.build(NotFound.T).text("Missing property: " + name).toMaybe();
	}
}
