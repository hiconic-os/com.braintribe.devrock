// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
package com.braintribe.artifacts.test.maven.settings.evaluation;

import java.io.File;

import com.braintribe.artifacts.test.maven.framework.FakeLocalRepositoryProvider;
import com.braintribe.artifacts.test.maven.framework.FakeMavenSettingsPersistenceExpertImpl;
import com.braintribe.build.artifact.representations.artifact.maven.settings.LocalRepositoryLocationProvider;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsExpertFactory;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.MavenSettingsPersistenceExpert;
import com.braintribe.logging.LoggerInitializer;
import com.braintribe.test.framework.TestUtil;

public abstract class AbstractMavenSettingsLab {	
	protected static File localRepository;	
	protected static MavenSettingsPersistenceExpert settingsPersistenceExpert;
	protected static LocalRepositoryLocationProvider localRepositoryLocationProvider;
	protected static MavenSettingsReader reader;
	
	//protected static File contents; 
	protected static File [] data;
	
	protected static void runBefore() {
		LoggerInitializer loggerInitializer = new LoggerInitializer();
		try {					
			loggerInitializer.setLoggerConfigUrl( new File("res/logger.properties").toURI().toURL());		
			loggerInitializer.afterPropertiesSet();
		} catch (Exception e) {		
			e.printStackTrace();
		}
		
		MavenSettingsExpertFactory mavenSettingsfactory = new MavenSettingsExpertFactory();
		if (settingsPersistenceExpert != null) {
			mavenSettingsfactory.setSettingsPeristenceExpert( settingsPersistenceExpert);
		}
		if (localRepositoryLocationProvider != null) {
			mavenSettingsfactory.setInjectedRepositoryRetrievalExpert(localRepositoryLocationProvider);
		}
		reader = mavenSettingsfactory.getMavenSettingsReader();
	}
			
	public static void before(File settings, File localRepository) {
		settingsPersistenceExpert = new FakeMavenSettingsPersistenceExpertImpl( settings);
		localRepositoryLocationProvider = new FakeLocalRepositoryProvider( localRepository);
		localRepository.mkdirs();
		
		runBefore();
		
		// clean local repository
		TestUtil.delete(localRepository);
		
	}
	
	protected static MavenSettingsReader getReader() {
		return reader;
	}
	 
}
