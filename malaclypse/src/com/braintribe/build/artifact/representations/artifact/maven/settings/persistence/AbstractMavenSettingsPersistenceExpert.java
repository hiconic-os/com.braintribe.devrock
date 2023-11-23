// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.maven.settings.persistence;

import java.io.File;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.marshaller.MavenSettingsMarshaller;
import com.braintribe.model.maven.settings.Mirror;
import com.braintribe.model.maven.settings.Profile;
import com.braintribe.model.maven.settings.Property;
import com.braintribe.model.maven.settings.Proxy;
import com.braintribe.model.maven.settings.Server;
import com.braintribe.model.maven.settings.Settings;

public abstract class AbstractMavenSettingsPersistenceExpert implements MavenSettingsPersistenceExpert {
	private MavenSettingsMarshaller marshaller = new MavenSettingsMarshaller();
		
	
	/**
	 * inject a synthetic property that marks the origin of the setting 
	 * @param settings - the settings to mark 
	 * @param file - the file that is the origin of the settings
	 */
	protected void markSettings( Settings settings, File file) {
		for (Profile profile : settings.getProfiles()) {
			Property property = Property.T.create();
			property.setName(MC_ORIGIN);
			property.setRawValue( file.getAbsolutePath());
			profile.getProperties().add( property);				
		}		
	}
	
	/**
	 * read the settings from the given file 
	 * @param contents - the {@link File} 
	 * @return - {@link Settings} as read 
	 * @throws RepresentationException - arrgh
	 */
	public Settings loadSettings( File contents) throws RepresentationException {
		try {		
			Settings settings = marshaller.unmarshall(contents);
			if (settings != null) {
				markSettings(settings, contents);
			}
			return settings; 
		} catch (XMLStreamException e) {
			throw new RepresentationException(e);
		}
	}
				
	/**
	 * merge two settings..<br/>
	 * merge the lists: if they have the same id, the one in the local one wins, otherwise copy the entries from the global
	 * <ul>
	 * <li>profiles</li>
	 * <li>mirrors</li>
	 * <li>servers</li>
	 * <li>pluginGroups></li>
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
	public Settings mergeSettings( Settings dominant, Settings recessive) {
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
		if (dominantPluginGroups.size() == 0) {
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
		if (dominantProxies.size() == 0) {			
			dominant.setProxies( recessiveProxies);
		}
		else if (recessiveProxies.size() > 0) {			
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
		if (dominantServers.size() == 0) {			
			dominant.setServers( recessiveServers);
		}
		else if (recessiveServers.size() > 0) {			
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
		if (dominantMirrors.size() == 0) {			
			dominant.setMirrors( recessiveMirrors);
		}
		else if (recessiveMirrors.size() > 0) {		
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
		if (dominantProfiles.size() == 0) {			
			dominant.setProfiles( recessiveProfiles);
		}
		else if (recessiveMirrors.size() > 0) {			
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
		
		List<Profile> recessiveActiveProfiles = recessive.getActiveProfiles();
		List<Profile> dominantActiveProfiles = dominant.getActiveProfiles();
		if (recessiveActiveProfiles.size() > 0) {		
			dominantActiveProfiles.addAll( recessiveActiveProfiles);
		}
		
		return dominant;
	}
	
	protected Settings generateMinimalSettings() {
		Settings settings = Settings.T.create();
		settings.setLocalRepository( "${user.home}/.m2/repository");
		return settings;
	}
	

}
