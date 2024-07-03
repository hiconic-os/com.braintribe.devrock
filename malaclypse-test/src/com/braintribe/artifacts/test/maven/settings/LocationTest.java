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
package com.braintribe.artifacts.test.maven.settings;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.build.artifact.representations.RepresentationException;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsExpertFactory;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.model.malaclypse.cfg.repository.RemoteRepository;
import com.braintribe.model.maven.settings.Profile;
import com.braintribe.testing.category.SpecialEnvironment;

/**
 * note . test requires :
 * 	a) a settings.xml
 *  b) that contains active profiles 
 *  c) that contains repositories within active profiles 
 * @author pit
 *
 */
@Category(SpecialEnvironment.class)
public class LocationTest {

	@Test
	public void test() {
		MavenSettingsExpertFactory factory = new MavenSettingsExpertFactory();
		MavenSettingsReader reader = factory.getMavenSettingsReader();
		
		System.out.println("*** active profiles ***");
		try {
			List<Profile> profiles = reader.getActiveProfiles();
			if (profiles != null) {
				for (Profile profile : profiles) {
					String profileId = profile.getId();
					System.out.println( profileId);
				}
			}
		} catch (RepresentationException e) {
			Assert.fail("exception [" + e + "] thrown");
		}
		
		System.out.println("*** local repository ***");
		try {
			System.out.println( reader.getLocalRepository(null));
		} catch (RepresentationException e) {
			Assert.fail("exception [" + e + "] thrown");
		}
		
		System.out.println("*** active remote repositories ***");
		try {
			List<RemoteRepository> repositories = factory.getMavenSettingsReader().getActiveRemoteRepositories();
			for (RemoteRepository repo : repositories) {
				System.out.println( repo.getName());
			}
		} catch (RepresentationException e) {
			Assert.fail("exception [" + e + "] thrown");
		}
		System.out.println("*** all declared remote repositories ***");
		try {
			List<RemoteRepository> repositories = factory.getMavenSettingsReader().getAllRemoteRepositories();
			for (RemoteRepository repo : repositories) {
				System.out.println( repo.getName());
			}
		} catch (RepresentationException e) {
			Assert.fail("exception [" + e + "] thrown");
		}
	}

}
