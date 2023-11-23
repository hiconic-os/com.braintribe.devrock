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
package com.braintribe.model.malaclypse.monitor;

import java.util.List;
import java.util.Map;

import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.generic.GenericEntity;

/**
 * a container for the PomParentDictionary as defined in Malaclypse
 * 
 * @author pit
 *
 */

public interface PomParentDictionaryContainer extends GenericEntity {
	
	public void setName( String name);
	public String getName();
	
	public void setArtifact( Artifact artifact);
	public Artifact getArtifact();
	
	public void setMissingArtifact( Artifact artifact);
	public Artifact getMissingArtifact();
	
	public void setRequestingArtifact( Artifact artifact);
	public Artifact getRequestingArtifact();
	
	public void setDeclarations( List<Dependency> declarations);
	public List<Dependency> getDeclarations();
	
	public List<PomParentDictionaryContainer> getDependencyLookupDelegates();
	public void setDependencyLookupDelegates( List<PomParentDictionaryContainer> delegates);	
	
	public void setProperties( Map<String, PomPropertyTuple> map);
	public Map<String, PomPropertyTuple> getProperties();
	
	public void setPomParentDictionaryContainer( PomParentDictionaryContainer parentContainer);
	public PomParentDictionaryContainer getPomParentDictionaryContainer();
	
}
