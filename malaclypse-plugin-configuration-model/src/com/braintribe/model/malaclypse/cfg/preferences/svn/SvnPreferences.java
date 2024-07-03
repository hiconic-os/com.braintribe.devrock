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
package com.braintribe.model.malaclypse.cfg.preferences.svn;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface SvnPreferences extends GenericEntity{
	
	final EntityType<SvnPreferences> T = EntityTypes.T(SvnPreferences.class);

	// obsolete : single svn working copy 
	String getWorkingCopy();
	void setWorkingCopy( String workingCopy);
	
	// obsolete : single svn working copy 
	String getUrl();
	void setUrl( String url);
	
	List<SourceRepositoryPairing> getSourceRepositoryPairings();
	void setSourceRepositoryPairings( List<SourceRepositoryPairing> sourceRepositories);
	
}
