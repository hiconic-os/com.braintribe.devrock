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
