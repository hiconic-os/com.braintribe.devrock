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
package com.braintribe.devrock.mc.core.compiler.configuration.environment;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.configuration.ConfigurableRepositoryConfigurationLoader;
import com.braintribe.devrock.mc.core.configuration.RepositoryConfigurationLoader;
import com.braintribe.devrock.mc.core.configuration.RepositoryConfigurationLocators;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

/**
 * tests whether the {@link RepositoryConfigurationLoader} correctly handles environment variables and system properties
 *  
 * @author pit
 *
 */
@Category( KnownIssue.class)
public class RepositoryConfigurationLoaderEnvironmentTest implements HasCommonFilesystemNode {
	protected File preparedInitialRepository;// = new File( getRoot(), "initial");	
	protected File input;
	protected File output;
			
	{
		Pair<File,File> pair = filesystemRoots("compiler/configuration/environment");
		input = pair.first;
		output = pair.second;
		
		preparedInitialRepository = new File( input, "initial");
				
	}
		
	@Before
	public void runBefore() {
		TestUtils.ensure(output);
		
		if (preparedInitialRepository.exists()) {
			TestUtils.copy(preparedInitialRepository, output);
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
		
		ConfigurableRepositoryConfigurationLoader loader = new ConfigurableRepositoryConfigurationLoader();
		loader.setVirtualEnvironment(ove);
		
		File cfg = new File( output, RepositoryConfigurationLoader.FILENAME_REPOSITORY_CONFIGURATION);		
		loader.setLocator(RepositoryConfigurationLocators.build().addRequiredLocation(cfg));

		
		Maybe<RepositoryConfiguration> maybe = loader.get();
		
		if (maybe.isUnsatisfied()) {
			Assert.fail("cannot retrieve configuration specified by [" + cfg + "] " + maybe.whyUnsatisfied().asFormattedText());
		}
		RepositoryConfiguration rcfg = maybe.get();
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
