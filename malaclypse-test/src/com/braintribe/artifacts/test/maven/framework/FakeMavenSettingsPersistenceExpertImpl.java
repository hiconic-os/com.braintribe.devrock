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
package com.braintribe.artifacts.test.maven.framework;

import java.io.File;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.AbstractMavenSettingsPersistenceExpert;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.MavenSettingsPersistenceExpert;
import com.braintribe.model.maven.settings.Settings;

/**
 * loads the two settings xml from the res directory of the local test environment
 * @author pit
 *
 */
public class FakeMavenSettingsPersistenceExpertImpl extends AbstractMavenSettingsPersistenceExpert implements MavenSettingsPersistenceExpert {
	File [] files;
	
	public FakeMavenSettingsPersistenceExpertImpl(File ...files) {
		this.files = files;
	}
	@Override
	public Settings loadSettings() throws RepresentationException {

		if (files.length > 1) {
			Settings localSettings = loadSettings( files[0]);
			Settings globalSettings = loadSettings(files[1]);
			return mergeSettings(localSettings, globalSettings);
		}
		else {
			return loadSettings( files[0]);
		}
	}
	
}
