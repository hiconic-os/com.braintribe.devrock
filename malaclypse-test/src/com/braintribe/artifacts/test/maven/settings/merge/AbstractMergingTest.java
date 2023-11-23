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
