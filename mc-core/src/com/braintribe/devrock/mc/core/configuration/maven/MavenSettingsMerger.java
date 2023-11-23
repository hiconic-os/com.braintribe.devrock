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
package com.braintribe.devrock.mc.core.configuration.maven;

import java.util.Arrays;
import java.util.List;

import com.braintribe.devrock.model.mc.cfg.origination.RepositoryConfigurationMerged;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.model.artifact.maven.settings.Mirror;
import com.braintribe.model.artifact.maven.settings.Profile;
import com.braintribe.model.artifact.maven.settings.Proxy;
import com.braintribe.model.artifact.maven.settings.Server;
import com.braintribe.model.artifact.maven.settings.Settings;

/**
 * merges two (or several) {@link Settings} into a single one. 
 * 
 * @author pit
 *
 */
public class MavenSettingsMerger {
	
	
	/**
	 * merges settings, first one being dominant, other merges as recessives
	 * @param settingsToMerge - the {@link Settings} to merge 
	 * @return - a merged {@link Settings}
	 */
	public static Settings mergeSettings( Settings ... settingsToMerge) {
		return mergeSettings( Arrays.asList( settingsToMerge));
	}

	/**
	 * merges settings, first one being dominant, other merges as recessives
	 * @param settingsToMerge - the {@link Settings} to merge 
	 * @return - a merged {@link Settings}
	 */
	public static Settings mergeSettings( List<Settings> settingsToMerge) {
		if (settingsToMerge == null) {
			return null;
		}
		if (settingsToMerge.size() == 1) {
			return settingsToMerge.get(0);
		}
		else {
			return mergeSettings( settingsToMerge.get(0), settingsToMerge.subList(1, settingsToMerge.size()));
		}
	}

	/**
	 * recursively merge {@link Settings}, the first being the dominant one
	 * @param dominant - the dominant {@link Settings}
	 * @param settingsToMerge - the settings to be merged as recessive {@link Settings}
	 * @return - the merged {@link Settings}
	 */
	public static Settings mergeSettings( Settings dominant, List<Settings> settingsToMerge) {
		if (settingsToMerge == null || settingsToMerge.size() == 0) {
			return dominant;
		}
		Settings merged = mergeSettings(dominant, settingsToMerge.get(0));
		if (settingsToMerge.size() == 1)
			return merged;
		
		List<Settings> settings = settingsToMerge.subList(1, settingsToMerge.size());
		return mergeSettings( merged, settings);		
	}
	
	/**
	 * merge two settings..<br/>
	 * merge the lists: if they have the same id, the one in the dominant one wins, otherwise copy the entries from the recessive
	 * <ul>
	 * <li>profiles</li>
	 * <li>mirrors</li>
	 * <li>servers</li>
	 * <li>pluginGroups</li>
	 * <li>proxies</li>
	 * </ul>
	 * <br/>
	 * merge the singleton: if they exist in the dominant, don't do anything
	 * <ul>
	 * <li>localRepository</li>
	 * <li>interactiveMode</li>
	 * <li>usePluginRegistry</li>
	 * <li>offline</li>
	 * </ul>
	 * <br/>  
	 * @param dominant - the dominant {@link Settings}, from the local (user) settings.xml 
	 * @param recessive - the recessive {@link Settings}, from the global (m2 home) settings.xml
	 * @return - the combined {@link Settings}
	 */
	public static Settings mergeSettings( Settings dominant, Settings recessive) {
		if (dominant == null) {
			if (recessive == null) {
				throw new IllegalStateException("no settings found to merge");
			}
			return recessive;
		}
		else {
			if (recessive == null) {
				return dominant;
			}
		}
		// 
		if (dominant.getInteractiveMode() == null) 
			dominant.setInteractiveMode( recessive.getInteractiveMode());
		
		if (dominant.getOffline() == null)
			dominant.setOffline( recessive.getOffline());
		
		if (dominant.getLocalRepository() == null)
			dominant.setLocalRepository( recessive.getLocalRepository());
		
		if (dominant.getUsePluginRegistry() == null)
			dominant.setUsePluginRegistry(recessive.getUsePluginRegistry());
		
		// plugins (not support yet, but still)
		List<String> dominantPluginGroups = dominant.getPluginGroups();
		List<String> recessivePluginGroups = recessive.getPluginGroups();
		if (dominantPluginGroups != null && dominantPluginGroups.size() == 0) {
			dominant.setPluginGroups( recessivePluginGroups);
		} 
		else if (recessivePluginGroups != null) {			
			for (String recessivePlugin : recessivePluginGroups) {
				if (!dominantPluginGroups.contains( recessivePlugin))
					dominantPluginGroups.add(recessivePlugin);
			}			
		}
		// proxies
		List<Proxy> dominantProxies = dominant.getProxies();
		List<Proxy> recessiveProxies = recessive.getProxies();
		if (dominantProxies != null && dominantProxies.size() == 0) {			
			dominant.setProxies( recessiveProxies);
		}
		else if (recessiveProxies != null && recessiveProxies.size() > 0) {			
			for (Proxy recessiveProxy : recessiveProxies) {
				String id = recessiveProxy.getId();
				boolean present = false;
				for (Proxy dominantProxy : dominantProxies) {
					String dominantProxyId = dominantProxy.getId();
					if (dominantProxyId.equalsIgnoreCase(id)) {
						present = true;
						break;
					}
				}
				if (!present) {
					dominantProxies.add(recessiveProxy);
				}				
			}
		}
		// servers
		List<Server> dominantServers = dominant.getServers();
		List<Server> recessiveServers = recessive.getServers();
		if (dominantServers != null && dominantServers.size() == 0) {			
			dominant.setServers( recessiveServers);
		}
		else if (recessiveServers != null && recessiveServers.size() > 0) {			
			for (Server recessiveServer : recessiveServers) {
				String id = recessiveServer.getId();
				boolean present = false;
				for (Server dominantServer : dominantServers) {
					String dominantServerId = dominantServer.getId();
					if (id.equalsIgnoreCase(dominantServerId)) {
						present = true;
						break;
					}
				}
				if (!present) {
					dominantServers.add(recessiveServer);
				}				
			}
		}
		// mirrors
		List<Mirror> dominantMirrors = dominant.getMirrors();
		List<Mirror> recessiveMirrors = recessive.getMirrors();
		if (dominantMirrors != null && dominantMirrors.size() == 0) {			
			dominant.setMirrors( recessiveMirrors);
		}
		else if (recessiveMirrors != null && recessiveMirrors.size() > 0) {		
			for (Mirror recessiveMirror : recessiveMirrors) {
				String id = recessiveMirror.getId();
				boolean present = false;
				for (Mirror dominantMirror : dominantMirrors) {
					String dominantMirrorId = dominantMirror.getId();
					if (dominantMirrorId.equalsIgnoreCase(id)) {
						present = true;
						break;
					}
				}
				if (!present) {
					dominantMirrors.add(recessiveMirror);
				}				
			}
		}

		// profiles
		List<Profile> dominantProfiles = dominant.getProfiles();
		List<Profile> recessiveProfiles= recessive.getProfiles();
		if (dominantProfiles != null && dominantProfiles.size() == 0) {			
			dominant.setProfiles( recessiveProfiles);
		}
		else {
			if (recessiveProfiles != null) {
				for (Profile recessiveProfile : recessiveProfiles) {
					String id = recessiveProfile.getId();
					boolean present = false;
					for (Profile dominantProfile : dominantProfiles) {
						String dominantProfileId = dominantProfile.getId();
						if (dominantProfileId.equalsIgnoreCase(id)) {
							present = true;
							break;
						}
					}
					if (!present) {
						dominantProfiles.add(recessiveProfile);
					}				
				}
			}
		}		
		
		List<Profile> recessiveActiveProfiles = recessive.getActiveProfiles();
		List<Profile> dominantActiveProfiles = dominant.getActiveProfiles();
		if (recessiveActiveProfiles != null && recessiveActiveProfiles.size() > 0) {		
			dominantActiveProfiles.addAll( recessiveActiveProfiles);
		}
		
		// remove the simple origination of the dominant and replace by merged
		dominant.setOrigination( Reasons.build(RepositoryConfigurationMerged.T).text( "merged").causes( dominant.getOrigination(), recessive.getOrigination()).toReason());
		
		return dominant;
	}
}
