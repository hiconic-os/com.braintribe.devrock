package com.braintribe.build.artifact.representations.artifact.maven.settings.persistence;

import java.io.File;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.model.maven.settings.Settings;

/**
 * simple {@link MavenSettingsPersistenceExpert} where you can specify the settings.xml to be used (actually you can still have the two settings)
 * @author pit
 *
 */
public class ExplicitFileSettingsPersistenceExpert extends AbstractMavenSettingsPersistenceExpert implements MavenSettingsPersistenceExpert {

	private File userSettingsFile;
	private File installationSettingsFile;
	
	/**
	 * single file 
	 * @param settings - the {@link File} that points to the settings.xml
	 */
	public ExplicitFileSettingsPersistenceExpert(File settings) {
		userSettingsFile = settings;
	}
	
	/**
	 * two files plus merging 
	 * @param userSettings - the dominant settings {@link File} (user specific)
	 * @param installationSettings - the recessive {@link File} (installation specific)
	 */
	public ExplicitFileSettingsPersistenceExpert(File userSettings, File installationSettings) {
		userSettingsFile = userSettings;
		installationSettingsFile = installationSettings;
	}
	
	@Override
	public Settings loadSettings() throws RepresentationException {
		if (installationSettingsFile != null) {
			Settings userSettings = loadSettings(userSettingsFile);
			Settings installationSettings = loadSettings(installationSettingsFile);
			return mergeSettings(userSettings, installationSettings);
		}
		else {
			return loadSettings(userSettingsFile);
		}		
	}

}
