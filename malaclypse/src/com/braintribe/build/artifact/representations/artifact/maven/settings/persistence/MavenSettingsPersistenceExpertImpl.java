// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.artifact.representations.artifact.maven.settings.persistence;

import java.io.File;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;
import com.braintribe.model.maven.settings.Settings;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

/**
 * a low level reader for Maven's settings.xml 
 * reacts on overriding environment variables or uses the standard maven style to find them
 * 
 * @author pit
 *
 */
public class MavenSettingsPersistenceExpertImpl extends AbstractMavenSettingsPersistenceExpert implements MavenSettingsPersistenceExpert {
	private static Logger log = Logger.getLogger(MavenSettingsPersistenceExpertImpl.class);
	private static final String ENV_PREFIX = "ARTIFACT_REPOSITORIES";
	public static final String ENV_SETTINGS = ENV_PREFIX + "_EXCLUSIVE_SETTINGS";
	public static final String ENV_LOCAL_SETTINGS = ENV_PREFIX + "_USER_SETTINGS";
	public static final String ENV_GLOBAL_SETTINGS = ENV_PREFIX + "_GLOBAL_SETTINGS";
	private final static String M2HOME = "M2_HOME";
	private final static String USERHOME = "user.home";
	private VirtualEnvironment virtualEnvironment = StandardEnvironment.INSTANCE;
	private boolean leniency = false;
	
	@Configurable
	public void setLeniency(boolean leniency) {
		this.leniency = leniency;
	}
	@Configurable
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}

	/* (non-Javadoc)
	 * @see com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsPersistenceExpert#loadSettings()
	 */
	@Override
	public Settings loadSettings() throws RepresentationException {
		String settings = virtualEnvironment.getEnv(ENV_SETTINGS);
		
		if (settings != null ) {
			File settingsFile = new File( settings);
			if (settingsFile.exists() && settingsFile.canRead()) {
				return loadSettings( settingsFile);
			}
			else {
				if (leniency) {
					log.warn( "environment variable [" + ENV_SETTINGS + "] exists, but its value [" + settingsFile.getAbsolutePath() + "] doesn't exist. Fallback to standard");
				}
				else {
					String msg = "environment variable [" + ENV_SETTINGS + "] exists, but its value [" + settingsFile.getAbsolutePath() + "] doesn't exist.";
					log.error( msg);
					throw new IllegalStateException( msg);
				}
			}
		}		
		return loadSettingsPerDefault();				
	}
	
	public Settings loadSettingsPerDefault() throws RepresentationException {
		File localSettingsDocument = getLocalSettings();
		File globalSettingsDocument = getGlobalSettings();
		
		if (localSettingsDocument == null && globalSettingsDocument == null) {
			String msg = String.format("No settings.xml found, neither in overrides [" + ENV_LOCAL_SETTINGS + "," + ENV_GLOBAL_SETTINGS +"] nor in [${user.home}/.m2] nor in [${M2_HOME}/conf], injecting minimal settings");
			if (leniency) {
				log.warn( msg);
				return generateMinimalSettings();				
			}
			else {
				log.error( msg);
				throw new IllegalStateException(msg);
			}
		}
		Settings settings;
		if (localSettingsDocument != null && globalSettingsDocument != null) {
			Settings localSettings = loadSettings(localSettingsDocument);
			Settings globalSettings = loadSettings(globalSettingsDocument);
			settings = mergeSettings(localSettings, globalSettings);
		} 
		else if (localSettingsDocument != null){
			settings = loadSettings(localSettingsDocument);
		}
		else {
			settings = loadSettings(globalSettingsDocument);
		}
			
		return settings;
	}
	


	/**
	 * get the settings.xml from the global maven location - ${M2_HOME}/conf
	 * @return - the {@link File} that represents global settings 
	 */
	private File getGlobalSettings() {
		String globalSettingsFile = virtualEnvironment.getEnv( ENV_GLOBAL_SETTINGS );
		if (globalSettingsFile != null) {
			File result = new File( globalSettingsFile);
			if (result.exists()) {
				return result;
			}
			else {
				if (leniency) {
					log.warn( "environment variable [" + ENV_GLOBAL_SETTINGS + "] exists, put its value [" + globalSettingsFile + "] doesn't exist. Fallback to standard");
				}
				else {
					String msg = "environment variable [" + ENV_GLOBAL_SETTINGS + "] exists, put its value [" + globalSettingsFile + "] doesn't exist.";
					log.error( msg);
					throw new IllegalStateException( msg);
				}
			}
		}		
		String	home = virtualEnvironment.getEnv(M2HOME);		
		if (home == null) {
			return null;
		}
		
		String path = home + "/conf/settings.xml";
		try {
			File result = new File( path);
			if (result.exists()) {
				return result;
			}			
		} catch (Exception e) {
			;
		}				
		return null;
	}
	
	/**
	 * get the settings.xml from the local maven location - ${user.home}/.m2
	 * @return - the {@link File} that represents the local settings
	 */
	private File getLocalSettings() {
		String localSettingsFile = virtualEnvironment.getEnv( ENV_LOCAL_SETTINGS);
		if (localSettingsFile != null) {
			File result = new File( localSettingsFile);
			if (result.exists()) {
				return result;
			}
			else {
				if (leniency) {
					log.warn( "environment variable [" + ENV_LOCAL_SETTINGS + "] exists, put its value [" + localSettingsFile + "] doesn't exist. Fallback to standard");
				}
				else {
					String msg = "environment variable [" + ENV_LOCAL_SETTINGS + "] exists, put its value [" + localSettingsFile + "] doesn't exist. Fallback to standard";
					log.error( msg);
					throw new IllegalStateException(msg);
				}
			}
		}
		
		String home = virtualEnvironment.getProperty( USERHOME);		
		if (home == null)
			return null;
		String path = home + "/.m2/settings.xml";
		try {
			File result = new File( path);
			if (result.exists())
				return result;
		} catch (Exception e) {
			;
		}		
		return null;
	}
	
	
	
}
