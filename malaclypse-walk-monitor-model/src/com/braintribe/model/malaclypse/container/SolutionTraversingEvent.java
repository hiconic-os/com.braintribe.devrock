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
package com.braintribe.model.malaclypse.container;

import java.util.List;

import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface SolutionTraversingEvent extends TraversingEvent {
	
	final EntityType<SolutionTraversingEvent> T = EntityTypes.T(SolutionTraversingEvent.class);

	void setArtifact( Artifact solution);
	Artifact getArtifact();
	
	void setParent( Dependency parent);
	Dependency getParent();
	
	void setLocation( String location);
	String getLocation();
	
	boolean getValidity();
	void setValidity( boolean validity);
	
	boolean getInjectedPerRedirection();
	void setInjectedPerRedirection( boolean injected);
	
	boolean getParentNature();
	void setParentNature( boolean parentNature);
	
	boolean getImportNature();
	void setImportNature( boolean importNature);
	
	List<String> getFailedImports();
	void setFailedImports( List<String> failedImports);
}
