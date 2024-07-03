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
