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
package com.braintribe.artifacts.test.maven.settings.merge;

import java.io.File;

import org.junit.Assert;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.persistence.MavenSettingsPersistenceExpertImpl;
import com.braintribe.model.maven.settings.Settings;

public abstract class AbstractMergingTest {
	
	private File settingsDir = new File( "res/maven/settings");
	protected File mergeDir = new File( settingsDir, "merging");


	protected Settings testMerging(File dominantFile, File recessiveFile) {
		
		
		MavenSettingsPersistenceExpertImpl loader = new MavenSettingsPersistenceExpertImpl();
						
		
		
		Settings dominantSettings = null;
		try {
			dominantSettings = (Settings) loader.loadSettings( dominantFile);
		} catch (RepresentationException e) {
			e.printStackTrace();
			String msg = String.format("cannot decode xml from [%s] ", dominantFile.getAbsolutePath());
			Assert.fail( msg);
			return null;
		}
		
		Settings recessiveSettings = null;
		try {
			recessiveSettings = (Settings) loader.loadSettings( recessiveFile);
		} catch (RepresentationException e) {
			e.printStackTrace();
			String msg = String.format("cannot decode xml from [%s] ", recessiveFile.getAbsolutePath());
			Assert.fail( msg);
			return null;
		}
		
		
		Settings combinedSettings = loader.mergeSettings(dominantSettings, recessiveSettings);
		return combinedSettings;
		
	}
	
	public boolean validate( Settings settings) {return true;};
}
