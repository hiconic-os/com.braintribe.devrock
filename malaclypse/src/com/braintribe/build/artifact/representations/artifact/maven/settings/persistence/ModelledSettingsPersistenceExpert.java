package com.braintribe.build.artifact.representations.artifact.maven.settings.persistence;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.model.maven.settings.Settings;

/**
 * a very simple {@link MavenSettingsPersistenceExpert} where you can actually add the assembly of a {@link Settings}
 * @author pit
 *
 */
public class ModelledSettingsPersistenceExpert implements MavenSettingsPersistenceExpert {
	private Settings settings;
	
	/**
	 * @param settings - an assembly of an {@link Settings} to be used
	 */
	public ModelledSettingsPersistenceExpert(Settings settings) {
		this.settings = settings;
	}

	@Override
	public Settings loadSettings() throws RepresentationException {
		return settings;
	}

}
