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


/**
 * the main settings 
 * @author pit
 *
 */
public interface Settings extends com.braintribe.model.generic.GenericEntity {
	
	final EntityType<Settings> T = EntityTypes.T(Settings.class);

	public static final String activeProfiles = "activeProfiles";
	public static final String id = "id";
	public static final String interactiveMode = "interactiveMode";
	public static final String localRepository = "localRepository";
	public static final String mirrors = "mirrors";
	public static final String offline = "offline";
	public static final String pluginGroups = "pluginGroups";
	public static final String profiles = "profiles";
	public static final String proxies = "proxies";
	public static final String servers = "servers";
	public static final String usePluginRegistry = "usePluginRegistry";

	void setActiveProfiles(List<Profile> value);
	List<Profile> getActiveProfiles();

	void setInteractiveMode(java.lang.Boolean value);
	java.lang.Boolean getInteractiveMode();

	void setLocalRepository(java.lang.String value);
	java.lang.String getLocalRepository();

	void setMirrors(List<Mirror> value);
	List<Mirror> getMirrors();

	void setOffline(java.lang.Boolean value);
	java.lang.Boolean getOffline();

	void setPluginGroups(List<String> pluginGroups);
	List<String> getPluginGroups();

	void setProfiles(List<Profile> value);
	List<Profile> getProfiles();

	void setProxies(List<Proxy> value);
	List<Proxy> getProxies();

	void setServers(List<Server> value);
	List<Server> getServers();
	
	void setUsePluginRegistry(java.lang.Boolean value);
	java.lang.Boolean getUsePluginRegistry();

}
