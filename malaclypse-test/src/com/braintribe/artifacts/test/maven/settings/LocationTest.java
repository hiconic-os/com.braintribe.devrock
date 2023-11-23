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
