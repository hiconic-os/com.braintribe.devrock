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
package com.braintribe.model.artifact.maven.settings;

import java.util.List;

import com.braintribe.devrock.model.mc.cfg.origination.Origination;
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
	public static final String interactiveMode = "interactiveMode";
	public static final String localRepository = "localRepository";
	public static final String mirrors = "mirrors";
	public static final String offline = "offline";
	public static final String pluginGroups = "pluginGroups";
	public static final String profiles = "profiles";
	public static final String proxies = "proxies";
	public static final String servers = "servers";
	public static final String usePluginRegistry = "usePluginRegistry";
	String standardMavenCascadeResolved = "standardMavenCascadeResolved";

	void setActiveProfiles(List<Profile> value);
	/**
	 * @return - the profiles listed as active
	 */
	List<Profile> getActiveProfiles();

	void setInteractiveMode(java.lang.Boolean value);
	/**
	 * @return - the interactive mode (would control Maven's logging output)
	 */
	java.lang.Boolean getInteractiveMode();

	void setLocalRepository(java.lang.String value);
	/**
	 * @return - the path to the 'local repository'
	 */
	java.lang.String getLocalRepository();

	void setMirrors(List<Mirror> value);
	/**
	 * @return - the declared {@link Mirror}
	 */
	List<Mirror> getMirrors();

	void setOffline(java.lang.Boolean value);
	/**
	 * @return - the global offline mode
	 */
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
	
	/**
	 * @return - true if resolved using the standard maven locations
	 */
	boolean getStandardMavenCascadeResolved();
	void setStandardMavenCascadeResolved(boolean standardMavenCascadeResolved);

	
	/**
	 * @return - the {@link Origination}, i.e how it came into being
	 */
	Origination getOrigination();
	void setOrigination(Origination value);

}
