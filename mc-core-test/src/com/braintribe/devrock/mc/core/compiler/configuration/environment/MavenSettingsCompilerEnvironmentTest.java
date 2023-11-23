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
package com.braintribe.devrock.mc.core.compiler.configuration.environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.configuration.RepositoryConfigurationLoader;
import com.braintribe.devrock.mc.core.configuration.maven.MavenSettingsCompiler;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.marshaller.artifact.maven.settings.DeclaredMavenSettingsMarshaller;
import com.braintribe.model.artifact.maven.settings.Settings;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

@Category(KnownIssue.class) 
public class MavenSettingsCompilerEnvironmentTest implements HasCommonFilesystemNode {
	protected File preparedInitialRepository;// = new File( getRoot(), "initial");	
	protected File input;
	protected File output;
			
	{
		Pair<File,File> pair = filesystemRoots("compiler/configuration/environment");
		input = pair.first;
		output = pair.second;
		
		preparedInitialRepository = new File( input, "initial");
				
	}
	private DeclaredMavenSettingsMarshaller marshaller = new DeclaredMavenSettingsMarshaller();
		
	@Before
	public void runBefore() {
		TestUtils.ensure(output);
		
		if (preparedInitialRepository.exists()) {
			TestUtils.copy(preparedInitialRepository, output);
		}
	}
	
	private Settings loadSettings( File file) {
		try (InputStream in = new FileInputStream( file)){			
			Settings settings = marshaller.unmarshall(in);		
			return settings;
		} catch (Exception e) {
			throw new IllegalStateException("cannot read file [" + file.getAbsolutePath() + "]", e);
		}
	}
	
	@Test
	public void test() {
		OverridingEnvironment ove = new OverridingEnvironment( StandardEnvironment.INSTANCE);
		// switch off any other environment variables
		ove.setEnv(RepositoryConfigurationLoader.ENV_DEVROCK_REPOSITORY_CONFIGURATION, null);
		ove.setProperty("user.home", "aint_my_home");
				
		
		// prep
		String path = new File( output, "repo").getAbsolutePath();
		ove.setEnv( "CACHE", path);
		
		String repositoryName = "myRepo";
		ove.setEnv( "repoName", repositoryName);
		
		String repositoryUser = "myUser";
		ove.setProperty( "user.name", repositoryUser);
		
		String repositoryPassword = "password";
		ove.setEnv( "repoPwd", repositoryPassword);
		
		String repositoryUrl = "https://myUrl";
		ove.setEnv( "repoUrl", repositoryUrl);
		
		MavenSettingsCompiler compiler = new MavenSettingsCompiler();
		Settings settings = loadSettings( new File( output, "settings.xml"));
		compiler.setVirtualEnvironment(ove);
		compiler.setSettingsSupplier( () -> settings);
		
				
		RepositoryConfiguration rcfg = compiler.get();
				
		//
		// test correct replacement
		// 
		String localRepositoryPath = rcfg.getLocalRepositoryPath();
		Assert.assertTrue("expected the local repository path to be [" + path + "], yet found [" + localRepositoryPath + "]", path.equals(localRepositoryPath));
		
		List<Repository> repositories = rcfg.getRepositories();
		Assert.assertTrue("expected [1] repositories, yet found [" + repositories.size() + "]", repositories.size() == 1);
		
		Repository repository = repositories.get(0);
		
		if (repository instanceof MavenHttpRepository == false) {
			Assert.fail("expected single repository to be of type [MavenHttpRepository], but it's [" + repositoryName.getClass().getSimpleName());
		}
		
		MavenHttpRepository mRepository = (MavenHttpRepository) repository;
		
		String name = mRepository.getName();
		Assert.assertTrue("expected the repository name to be [" + repositoryName + "], yet found [" + name + "]", repositoryName.equals( name));
		
		String user = mRepository.getUser();
		Assert.assertTrue("expected the repository user to be [" + repositoryUser + "], yet found [" + user + "]", repositoryUser.equals( user));
		
		String password = mRepository.getPassword();
		Assert.assertTrue("expected the repository password to be [" + repositoryPassword + "], yet found [" + password + "]", repositoryPassword.equals( password));
		
		String url = mRepository.getUrl();
		Assert.assertTrue("expected the repository url to be [" + repositoryUrl + "], yet found [" + url + "]", repositoryUrl.equals( url));			
	}

}
