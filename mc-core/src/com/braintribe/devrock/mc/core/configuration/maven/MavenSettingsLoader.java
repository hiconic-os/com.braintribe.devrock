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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.model.mc.cfg.origination.MinimalRepositoryConfigurationInjected;
import com.braintribe.devrock.model.mc.cfg.origination.RepositoryConfigurationLoaded;
import com.braintribe.devrock.model.mc.cfg.origination.RepositoryConfigurationLocated;
import com.braintribe.gm.reason.TemplateReasons;
import com.braintribe.logging.Logger;
import com.braintribe.marshaller.artifact.maven.settings.DeclaredMavenSettingsMarshaller;
import com.braintribe.model.artifact.maven.settings.Settings;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

/**
 * load the settings 'maven style': 
 * supports the standard maven location for user- and installation-configuration, 
 * plus the environment variables as per convention 
 * 
 * @author pit
 *
 */
public class MavenSettingsLoader implements  Supplier<Settings> {
	private static Logger log = Logger.getLogger(MavenSettingsLoader.class);
	private static final String ENV_PREFIX = "ARTIFACT_REPOSITORIES";
	public static final String ENV_EXCLUSIVE_SETTINGS = ENV_PREFIX + "_EXCLUSIVE_SETTINGS";
	public static final String ENV_LOCAL_SETTINGS = ENV_PREFIX + "_USER_SETTINGS";
	public static final String ENV_GLOBAL_SETTINGS = ENV_PREFIX + "_GLOBAL_SETTINGS";
	public final static String M2HOME = "M2_HOME";
	public final static String USERHOME = "user.home";
	private VirtualEnvironment virtualEnvironment = StandardEnvironment.INSTANCE;
	private boolean leniency = true;
	private static DeclaredMavenSettingsMarshaller marshaller = new DeclaredMavenSettingsMarshaller();
	
	@Configurable
	public void setLeniency(boolean leniency) {
		this.leniency = leniency;
	}
	
	@Configurable
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}
	
	@Override
	public Settings get() {	
		String settings = virtualEnvironment.getEnv(ENV_EXCLUSIVE_SETTINGS);
		
		if (settings != null ) {
			File settingsFile = new File( settings);
			if (settingsFile.exists() && settingsFile.canRead()) {
				Settings exclusiveSettings = loadSettings( settingsFile);
				exclusiveSettings.getOrigination().getReasons().add( TemplateReasons.build( RepositoryConfigurationLocated.T).assign(RepositoryConfigurationLocated::setExpression, ENV_EXCLUSIVE_SETTINGS).toReason());
				return exclusiveSettings;
			}
			else {
				if (leniency) {
					log.warn( "environment variable [" + ENV_EXCLUSIVE_SETTINGS + "] exists, but its value [" + settingsFile.getAbsolutePath() + "] doesn't exist. Fallback to standard");
				}
				else {
					String msg = "environment variable [" + ENV_EXCLUSIVE_SETTINGS + "] exists, but its value [" + settingsFile.getAbsolutePath() + "] doesn't exist.";
					log.error( msg);
					throw new IllegalStateException( msg);
				}
			}
		}		
		return loadSettingsPerDefault();				
	}
	
	/**
	 * simple loader : just read the declared {@link Settings} from the file
	 * @param contents - the {@link File} to read
	 * @return - the declared {@link Settings}
	 */
	public static Settings loadSettings( File contents)  {
		try (InputStream in = new FileInputStream(contents)){		
			Settings settings = (Settings) marshaller.unmarshall(in);				
			settings.setOrigination( TemplateReasons.build(RepositoryConfigurationLoaded.T).assign(RepositoryConfigurationLoaded::setUrl, contents.getAbsolutePath()).toReason());			
			return settings; 
		} catch (Exception e) {
			throw new IllegalStateException( "cannot read declared settings from [" + contents.getAbsolutePath() + "]", e);
		}
	}
	
	/**
	 * @return - generates a {@link Settings} that only contains 
	 */
	private Settings generateMinimalSettings() {
		Settings settings = Settings.T.create();
		settings.setLocalRepository( "${user.home}/.m2/repository");
		settings.setOrigination( TemplateReasons.build(MinimalRepositoryConfigurationInjected.T).assign(MinimalRepositoryConfigurationInjected::setLocalRepositoryPath, settings.getLocalRepository()).toReason());
		return settings;
	}
	
	/**
	 * load settings and mark them if loaded via standard maven locations 
	 * @return - {@link Settings} as per default location 
	 */
	private Settings loadSettingsPerDefault(){
		// if pair.second is false, the env variable has been used
		Pair<File,Boolean> localSettingsDocumentPair = getLocalSettings();		
		Pair<File,Boolean> globalSettingsDocumentPair = getGlobalSettings();
		
		if (localSettingsDocumentPair == null && globalSettingsDocumentPair == null) {
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
		if (localSettingsDocumentPair != null && globalSettingsDocumentPair != null) {
			Settings localSettings = loadSettings(localSettingsDocumentPair.first);
			if (!localSettingsDocumentPair.second) {
				localSettings.getOrigination().getReasons().add( TemplateReasons.build(RepositoryConfigurationLocated.T) //
						.assign( RepositoryConfigurationLocated::setExpression, ENV_LOCAL_SETTINGS)
						//.text( "pointed at by '" + ENV_LOCAL_SETTINGS + "'") //
						.toReason()); //
			}
			else {
				localSettings.getOrigination().getReasons().add( TemplateReasons.build(RepositoryConfigurationLocated.T) //
						.assign( RepositoryConfigurationLocated::setExpression, "{user.home}/.m2") //
						.toReason()); //
			}
			Settings globalSettings = loadSettings(globalSettingsDocumentPair.first);
			if (!globalSettingsDocumentPair.second) {
				globalSettings.getOrigination().getReasons().add( TemplateReasons.build(RepositoryConfigurationLocated.T) //
						.assign( RepositoryConfigurationLocated::setExpression, ENV_GLOBAL_SETTINGS) //						
						.toReason()); //
			}
			else {
				globalSettings.getOrigination().getReasons().add( TemplateReasons.build(RepositoryConfigurationLocated.T) //
						.assign(RepositoryConfigurationLocated::setExpression, "{M2_HOME}/conf") //
						.toReason()); //
			}
			settings = MavenSettingsMerger.mergeSettings(localSettings, globalSettings);
			settings.setStandardMavenCascadeResolved( localSettingsDocumentPair.second || globalSettingsDocumentPair.second);
		} 
		else if (localSettingsDocumentPair != null){
			settings = loadSettings(localSettingsDocumentPair.first);
			if (!localSettingsDocumentPair.second) {
				settings.getOrigination().getReasons().add( TemplateReasons.build(RepositoryConfigurationLocated.T) //
						.assign(RepositoryConfigurationLocated::setExpression, ENV_LOCAL_SETTINGS) //
						.toReason()); //
			}
			else {
				settings.getOrigination().getReasons().add( TemplateReasons.build(RepositoryConfigurationLocated.T) //
						.assign(RepositoryConfigurationLocated::setExpression, "{user.home}/.m2") //						
						.toReason()); //
			}
			settings.setStandardMavenCascadeResolved( localSettingsDocumentPair.second);
		}
		else {
			settings = loadSettings(globalSettingsDocumentPair.first);
			if (!globalSettingsDocumentPair.second) {
				settings.getOrigination().getReasons().add( TemplateReasons.build(RepositoryConfigurationLocated.T) //
						.assign(RepositoryConfigurationLocated::setExpression, ENV_GLOBAL_SETTINGS) //						
						.toReason()); //
			}
			else {
				settings.getOrigination().getReasons().add( TemplateReasons.build(RepositoryConfigurationLocated.T) //
						.assign(RepositoryConfigurationLocated::setExpression, "{M2_HOME}/conf") //						
						.toReason()); //
			}
			settings.setStandardMavenCascadeResolved(globalSettingsDocumentPair.second);
		}
			
		return settings;
	}
	
	/**
	 * get the settings.xml from the global maven location - ${M2_HOME}/conf
	 * @return - the {@link File} that represents global settings 
	 */
	private Pair<File,Boolean> getGlobalSettings() {
		String globalSettingsFile = virtualEnvironment.getEnv( ENV_GLOBAL_SETTINGS );
		if (globalSettingsFile != null) {
			File result = new File( globalSettingsFile);
			if (result.exists()) {
				return Pair.of( result, false);
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
				return Pair.of(result, true);
			}			
		} catch (Exception e) {
			String msg = "loading of [" + path + "] resulted in an error";
			log.error( msg,e);
		}				
		return null;
	}
	
	/**
	 * get the settings.xml from the local maven location - ${user.home}/.m2
	 * @return - the {@link File} that represents the local settings
	 */
	private Pair<File,Boolean> getLocalSettings() {
		String localSettingsFile = virtualEnvironment.getEnv( ENV_LOCAL_SETTINGS);
		if (localSettingsFile != null) {
			File result = new File( localSettingsFile);
			if (result.exists()) {
				return Pair.of(result, false);
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
				return Pair.of(result, true);
		} catch (Exception e) {
			String msg = "loading of [" + path + "] resulted in an error";
			log.error( msg,e);
		}		
		return null;
	}

}
