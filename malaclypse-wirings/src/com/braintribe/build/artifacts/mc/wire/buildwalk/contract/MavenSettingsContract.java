package com.braintribe.build.artifacts.mc.wire.buildwalk.contract;

import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.MavenSettingsPersistenceExpert;
import com.braintribe.wire.api.space.WireSpace;

/**
 * 
 * @author pit
 *
 */
public interface MavenSettingsContract  extends WireSpace {

	MavenSettingsPersistenceExpert settingsPersistenceExpert();
	LocalRepositoryLocationProvider localRepositoryLocationProvider();
}
