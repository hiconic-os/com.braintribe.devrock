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
package com.braintribe.model.maven.settings;

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;  


public interface Profile extends com.braintribe.model.generic.GenericEntity {
	
	final EntityType<Profile> T = EntityTypes.T(Profile.class);

	public static final String activation = "activation";
	public static final String id = "id";
	public static final String id1 = "id1";
	public static final String pluginRepositories = "pluginRepositories";
	public static final String properties = "properties";
	public static final String repositories = "repositories";

	void setActivation(com.braintribe.model.maven.settings.Activation value);
	com.braintribe.model.maven.settings.Activation getActivation();

		
	void setPluginRepositories(List<Repository> value);
	List<Repository> getPluginRepositories();

	void setProperties(List<Property> value);
	List<Property> getProperties();

	void setRepositories(List<Repository> value);
	List<Repository> getRepositories();

}
