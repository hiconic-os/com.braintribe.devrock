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

import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.artifact.declared.DeclaredDependency;
import com.braintribe.model.artifact.declared.ProcessingInstruction;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.version.VersionExpression;

/**
 * a fully qualified dependency (as opposed to the {@link DeclaredDependency} which may be incomplete)
 * 
 * @author pit
 *
 */
public interface CompiledDependency extends CompiledDependencyIdentification {
	
	EntityType<CompiledDependency> T = EntityTypes.T(CompiledDependency.class);
	
	String scope = "scope";
	String optional = "optional";
	String origin = "origin";
	String exclusions = "exclusions";
	String versionIsNegotiable = "versionIsNegotiable";
	String processingInstructions = "processingInstructions";
	String invalid = "invalid";
	String whyInvalid = "whyInvalid";
	String tags = "tags";
	
	/**	 
	 * @return - the scope as declared in the pom
	 */
	String getScope();
	void setScope( String scope);
	
	/**
	 * @return - the optional flag as declared in the pom
	 */
	boolean getOptional();
	void setOptional( boolean optional);
		

	/**
	 * @return - the {@link CompiledArtifact} that contains this dependency
	 */
	CompiledArtifact getOrigin();
	void setOrigin( CompiledArtifact origin);
	
	Set<String> getTags();
	void setTags(Set<String> tags);
	
	Map<PartIdentification, String> getParts();
	void setParts(Map<PartIdentification, String> parts);
	
	/**
	 * @return - the exclusions of the dependency 
	 */
	Set<ArtifactIdentification> getExclusions();
	void setExclusions( Set<ArtifactIdentification> exclusions);
	
	/**
	 * whether this dependencie's version is rather a 'hint' than an 'requirement'
	 * @return - true if a clash with another dependency is ok, false if it should raise an exception 
	 */
	boolean getVersionIsNegotiable();
	void setVersionIsNegotiable( boolean hardRequired);
	
	/**
	 * 
	 * @return - processing instructions attached the dependency in the pom
	 */
	List<ProcessingInstruction> getProcessingInstructions();
	void setProcessingInstructions(List<ProcessingInstruction> instructions);
	
	/**
	 * Marks the dependency as invalid to allow lenient processing. If true you may find a reason for the invalidity with {@link #getWhyInvalid()}
	 */
	boolean getInvalid();
	void setInvalid(boolean invalid);
	
	/**
	 * May holds a {@link Reason} for invalidity in case of {@link #getInvalid()} is true. 
	 */
	Reason getWhyInvalid();
	void setWhyInvalid(Reason reason);

	
	@Override
	default String asString() {		
		if (getType() != null) {
			return CompiledDependencyIdentification.super.asString() + "/" + PartIdentification.asString(this);
		}
		else {
			return CompiledDependencyIdentification.super.asString();
		}
	}
	
	default String partIdentificationAsString() {
		return PartIdentification.asString(this);
	}
	
	static CompiledDependency from(CompiledDependencyIdentification cdi) {
		return from(cdi, "compile");
	}
	
	static CompiledDependency from(CompiledDependencyIdentification cdi, String scope) {
		return create(cdi.getGroupId(), cdi.getArtifactId(), cdi.getVersion(), scope);
	}

	static CompiledDependency from(DeclaredDependency d) {
		return create(d.getGroupId(), d.getArtifactId(), VersionExpression.parse(d.getVersion()), d.getScope(), d.getClassifier(), d.getType());
	}
	
	static CompiledDependency from(VersionedArtifactIdentification cdi) {
		return from(cdi, "compile");
	}
	
	static CompiledDependency from(VersionedArtifactIdentification cdi, String scope) {
		return create(cdi.getGroupId(), cdi.getArtifactId(), VersionExpression.parse(cdi.getVersion()), scope);
	}
	
	static CompiledDependency create(String groupId, String artifactId, VersionExpression version, String scope) {
		CompiledDependency dep = CompiledDependency.T.create();
		dep.setGroupId(groupId);
		dep.setArtifactId(artifactId);
		dep.setVersion(version);
		// at this point, the scope may not be defaulted to compile
		/*
		 * if (scope == null) { dep.setScope( "compile"); } else { dep.setScope(scope);
		 * }
		 */
		dep.setScope( scope);
		return dep;
	}
	
	static CompiledDependency create(String groupId, String artifactId, VersionExpression version, String scope, String classifier, String type) {
		CompiledDependency dep = CompiledDependency.T.create();
		dep.setGroupId(groupId);
		dep.setArtifactId(artifactId);
		dep.setVersion(version);
		// at this point, the scope may not be defaulted to compile 		
		/*
		 * if (scope == null) { dep.setScope( "compile"); } else { dep.setScope(scope);
		 * }
		 */
		dep.setScope( scope);
		dep.setClassifier(classifier);
		dep.setType(type);		
		return dep;
	}
}
