package com.braintribe.build.artifacts.mc.wire.repositoryExtract.space;

import java.io.File;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.ExplicitFileSettingsPersistenceExpert;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.MavenSettingsPersistenceExpert;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.MavenSettingsPersistenceExpertImpl;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.GeneralConfigurationContract;
import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.MavenSettingsContract;
import com.braintribe.build.artifacts.mc.wire.repositoryExtract.contract.ExternalConfigurationContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class MavenSettingsSpace implements MavenSettingsContract {
	
	@Import
	GeneralConfigurationContract commons;
	
	@Import
	ExternalConfigurationContract externalConfiguration;
	
	@Managed
	public MavenSettingsPersistenceExpert settingsPersistenceExpert() {
		MavenSettingsPersistenceExpert bean = null;	
		if (externalConfiguration.settingsOverride() == null) {
			bean = new MavenSettingsPersistenceExpertImpl();
			((MavenSettingsPersistenceExpertImpl)bean).setVirtualEnvironment(commons.virtualEnvironment());
		}
		else {
			bean = new ExplicitFileSettingsPersistenceExpert( new File(externalConfiguration.settingsOverride()));			
		}
		return bean;
		
	}

	
	@Override
	public LocalRepositoryLocationProvider localRepositoryLocationProvider() {
		if (externalConfiguration.repositoryOverride() != null) {
			return new LocalRepositoryLocationProvider() {			
				@Override
				public String getLocalRepository(String expression) throws RepresentationException {
					return externalConfiguration.repositoryOverride();
				}
			};
		}
		else {
			return null;
		}
	}
	
		
}
