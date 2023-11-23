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
