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
package com.braintribe.model.malaclypse.cfg.repository;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface RemoteRepository extends GenericEntity{
	
	final EntityType<RemoteRepository> T = EntityTypes.T(RemoteRepository.class);

	String getUrl();
	void setUrl( String url);
	
	String getUser();
	void setUser( String user);
	
	String getPassword();
	void setPassword( String password);
	
	String getName();
	void setName( String name);
	
	String getProfileName();
	void setProfileName( String name);
		
	ContentSettings getReleaseSettings();
	void setReleaseSettings( ContentSettings settings);
	
	ContentSettings getSnapshotSettings();
	void setSnapshotSettings( ContentSettings settings);
	
		
}
