package com.braintribe.build.artifacts.mc.wire.buildwalk.space;

import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.MavenSettingsPersistenceExpertImpl;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.GeneralConfigurationContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.MavenSettingsContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class MavenSettingsSpace implements MavenSettingsContract {
	
	@Import
	GeneralConfigurationContract commons;
	
	@Managed
	public MavenSettingsPersistenceExpertImpl settingsPersistenceExpert() {
		MavenSettingsPersistenceExpertImpl bean = new MavenSettingsPersistenceExpertImpl();
		bean.setVirtualEnvironment(commons.virtualEnvironment());
		return bean;
	}

	
	@Override
	public LocalRepositoryLocationProvider localRepositoryLocationProvider() {	
		return null;
	}
	
		
}
