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
