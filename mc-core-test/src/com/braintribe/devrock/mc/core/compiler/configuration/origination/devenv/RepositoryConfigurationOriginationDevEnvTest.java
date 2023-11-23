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
package com.braintribe.devrock.mc.core.compiler.configuration.origination.devenv;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.compiler.configuration.origination.ReasoningHelper;
import com.braintribe.devrock.mc.core.configuration.ConfigurableRepositoryConfigurationLoader;
import com.braintribe.devrock.mc.core.configuration.RepositoryConfigurationLoader;
import com.braintribe.devrock.mc.core.configuration.RepositoryConfigurationLocators;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.utils.paths.UniversalPath;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

/**
 * tests the dev-env loader origination - and hence proper resolving of the configuration
 * 
 * a) direct location passed 
 * b) dev env root passed
 * c) DEVROCK_REPOSITORY_CONFIGURATION set
 * d) user home
 * @author pit
 *
 */
public class RepositoryConfigurationOriginationDevEnvTest implements HasCommonFilesystemNode {	
	protected File preparedInitialRepository;// = new File( getRoot(), "initial");
	
	protected File input;
	protected File output;
			
	{
		Pair<File,File> pair = filesystemRoots("compiler/origination/dev-env");
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

	
	
	
	/**
	 * tests the direct passing of the repository configuration to the {@link RepositoryConfigurationLoader}
	 */
	// TODO: dsc reactivate and fix check of origination
	// @Test
	public void testDirectLocationPassed() {
		OverridingEnvironment ove = new OverridingEnvironment( StandardEnvironment.INSTANCE);
		ove.setEnv(RepositoryConfigurationLoader.ENV_DEVROCK_REPOSITORY_CONFIGURATION, null);
		ove.setProperty("user.home", "aint_my_home");
		
		ConfigurableRepositoryConfigurationLoader loader = new ConfigurableRepositoryConfigurationLoader();
		loader.setVirtualEnvironment(ove);
		
		File cfg = new File( output, RepositoryConfigurationLoader.FILENAME_REPOSITORY_CONFIGURATION);		
		loader.setLocator(RepositoryConfigurationLocators.build().addRequiredLocation(cfg));
		
		Maybe<RepositoryConfiguration> maybe = loader.get();
		
		if (maybe.isUnsatisfied()) {
			Assert.fail("cannot retrieve configuration specified by [" + cfg + "] " + maybe.whyUnsatisfied().asFormattedText());
		}
		RepositoryConfiguration rcfg = maybe.get();
		
		// check origination		
		Reason origination = rcfg.getOrigination();
	
		String found = ReasoningHelper.getDeclarationFile(origination);
		String expected = cfg.getAbsolutePath();
		
		Assert.assertTrue("expected declaring file to be [" + expected + "], yet found [" + found, expected.equals(found));		
		
		System.out.println( origination.asFormattedText());				
	}
	
	/**
	 * tests the direct passing of the 'dev env root' to the {@link RepositoryConfigurationLoader}
	 */
	@Test
	public void testDevenvRootPassed() {
		OverridingEnvironment ove = new OverridingEnvironment( StandardEnvironment.INSTANCE);
		ove.setEnv(RepositoryConfigurationLoader.ENV_DEVROCK_REPOSITORY_CONFIGURATION, null);
		ove.setProperty("user.home", "aint_my_home");
		
		RepositoryConfigurationLoader loader = new RepositoryConfigurationLoader();
		loader.setVirtualEnvironment(ove);
		
		File devenvRoot = new File( output, "dev-env");
		loader.setDevelopmentEnvironmentRoot(devenvRoot);
		
		Maybe<RepositoryConfiguration> maybe = loader.get();
		
		if (maybe.isUnsatisfied()) {
			Assert.fail("cannot retrieve configuration specified by the dev env root [" + devenvRoot + "] " + maybe.whyUnsatisfied().asFormattedText());
		}
		RepositoryConfiguration rcfg = maybe.get();
		
		// check origination
		
		Reason origination = rcfg.getOrigination();
		
		String found = ReasoningHelper.getDeclarationFile(origination);
		String expected = UniversalPath.from( devenvRoot) //
				.push(RepositoryConfigurationLoader.FOLDERNAME_ARTIFACTS) //
				.push(RepositoryConfigurationLoader.FILENAME_REPOSITORY_CONFIGURATION) //
				.toFile().getAbsolutePath();
		
		Assert.assertTrue("expected declaring file to be [" + expected + "], yet found [" + found, expected.equals(found));		
		
		System.out.println( origination.asFormattedText());				
	}
	
	/**
	 * test using the single repository configuration environment variable that points to the cfg
	 */
	@Test
	public void testEnvVar() {
		OverridingEnvironment ove = new OverridingEnvironment( StandardEnvironment.INSTANCE);
		String cfgFilePath = new File(output, RepositoryConfigurationLoader.FILENAME_REPOSITORY_CONFIGURATION).getAbsolutePath();
		ove.setEnv(RepositoryConfigurationLoader.ENV_DEVROCK_REPOSITORY_CONFIGURATION, cfgFilePath);
		ove.setProperty("user.home", "aint_my_home");
		
		RepositoryConfigurationLoader loader = new RepositoryConfigurationLoader();
		loader.setVirtualEnvironment(ove);
		
	
		Maybe<RepositoryConfiguration> maybe = loader.get();
		
		if (maybe.isUnsatisfied()) {
			Assert.fail("cannot retrieve configuration specified by the env [" + RepositoryConfigurationLoader.ENV_DEVROCK_REPOSITORY_CONFIGURATION + "] " + maybe.whyUnsatisfied().asFormattedText());
		}
		RepositoryConfiguration rcfg = maybe.get();
		
		// check origination
		
		Reason origination = rcfg.getOrigination();
		String found = ReasoningHelper.getDeclarationFile(origination);
		String expected = cfgFilePath; 
		
		Assert.assertTrue("expected declaring file to be [" + expected + "], yet found [" + found, expected.equals(found));		
		System.out.println( origination.asFormattedText());				
	}
	
	/**
	 * test finding the repository configuration file in the standard 'user.home' place
	 */
	@Test
	public void testUserHome() {
		OverridingEnvironment ove = new OverridingEnvironment( StandardEnvironment.INSTANCE);
		ove.setEnv(RepositoryConfigurationLoader.ENV_DEVROCK_REPOSITORY_CONFIGURATION, null);
		String path = new File( output, "user.home").getAbsolutePath();
		ove.setProperty("user.home", path);
		
		RepositoryConfigurationLoader loader = new RepositoryConfigurationLoader();
		loader.setVirtualEnvironment(ove);		
		
		Maybe<RepositoryConfiguration> maybe = loader.get();
		
		if (maybe.isUnsatisfied()) {
			Assert.fail("cannot retrieve configuration specified by user.home [" + path + "] " + maybe.whyUnsatisfied().asFormattedText());
		}
		RepositoryConfiguration rcfg = maybe.get();
		
		// check origination
		
		Reason origination = rcfg.getOrigination();		
		String found = ReasoningHelper.getDeclarationFile(origination);
		
		String expected = UniversalPath.empty().push( path) //
				.push(RepositoryConfigurationLoader.FOLDERNAME_DEVROCK) //
				.push(RepositoryConfigurationLoader.FILENAME_REPOSITORY_CONFIGURATION) //
				.toFile().getAbsolutePath();
		
		Assert.assertTrue("expected declaring file to be [" + expected + "], yet found [" + found, expected.equals(found));		
		System.out.println( origination.asFormattedText());				
	}
}
