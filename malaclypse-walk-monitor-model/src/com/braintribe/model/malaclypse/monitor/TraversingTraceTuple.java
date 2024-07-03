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

import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.generic.GenericEntity;


public interface TraversingTraceTuple extends GenericEntity{

	public Artifact getArtifact();
	public void setArtifact( Artifact artifact);
	
	public Dependency getDependency();
	public void setDependency( Dependency dependency);
	
	public String getLocation();
	public void setLocation(String location);
	
	public int getLevel();
	public void setLevel( int level);
	
	public boolean getValidFlag();
	public void setValidFlag( boolean valid);
	
	public boolean getCachedFlag();
	public void setCachedFlag( boolean cached);
}
