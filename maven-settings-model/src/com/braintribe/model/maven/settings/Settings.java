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
