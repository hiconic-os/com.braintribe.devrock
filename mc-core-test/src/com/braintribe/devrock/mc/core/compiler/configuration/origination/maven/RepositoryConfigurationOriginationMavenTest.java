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
package com.braintribe.devrock.mc.core.compiler.configuration.origination.maven;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.compiler.configuration.origination.ReasoningHelper;
import com.braintribe.devrock.mc.core.configuration.maven.MavenSettingsCompiler;
import com.braintribe.devrock.mc.core.configuration.maven.MavenSettingsLoader;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

/**
 * tests the maven settings compiler's origination - hence also proper loading 
 * a) via 'exclusive' single settings.xml set via env-variable
 * b) via two env-variables, one for the 'local' (user) and 'global' (maven install) -> merging needs to take place 
 * c) via its location in 'user.home/.m2'
 * d) via its location in 'M2_HOME/confg'
 * e) via both user.home and M2_HOME -> merging needs to take place
 * 
 * @author pit
 *
 */
public class RepositoryConfigurationOriginationMavenTest implements HasCommonFilesystemNode {	
	protected File preparedInitialRepository;// = new File( getRoot(), "initial");
	
	protected File input;
	protected File output;
			
	{
		Pair<File,File> pair = filesystemRoots("compiler/origination/maven");
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
	 * single environment variable - exclusive
	 */
	@Test
	public void testDirectLocationPassed() {
		File cfg = new File( input, "exclusive.settings.xml");
		OverridingEnvironment ove = new OverridingEnvironment( StandardEnvironment.INSTANCE);
		ove.setEnv(MavenSettingsLoader.ENV_EXCLUSIVE_SETTINGS, cfg.getAbsolutePath()); // 
		
		// 'null'-out all other env settings
		ove.setEnv(MavenSettingsLoader.ENV_LOCAL_SETTINGS, null); //
		ove.setEnv(MavenSettingsLoader.ENV_GLOBAL_SETTINGS, null); //
		ove.setProperty( MavenSettingsLoader.USERHOME, "aint_user_home");
		ove.setEnv( MavenSettingsLoader.M2HOME, "aint_m2_home");
		
		
		MavenSettingsLoader loader = new MavenSettingsLoader();
		loader.setVirtualEnvironment(ove);
		
		MavenSettingsCompiler compiler = new MavenSettingsCompiler();
		compiler.setSettingsSupplier( loader);
		
		
		RepositoryConfiguration repositoryConfiguration = compiler.get();
								
		// check origination		
		Reason origination = repositoryConfiguration.getOrigination();
	
		// declaration
		String found = ReasoningHelper.getDeclarationFile(origination);
		String expected = cfg.getAbsolutePath();		
		Assert.assertTrue("expected declaring file to be [" + expected + "], yet found [" + found, expected.equals(found));
		
		// pointer
		List<String> pointers = ReasoningHelper.getPointers(origination);
		Assert.assertTrue("expected only a single pointer, yet found [" + pointers.size() + "]", pointers.size() == 1);
		
		String pointer = pointers.get(0);				
		Assert.assertTrue( "expected pointer to be [" + pointer + "], yet found [" + pointer + "]", MavenSettingsLoader.ENV_EXCLUSIVE_SETTINGS.equals(pointer));
		
				
		System.out.println( origination.stringify());				
	}
	
	
	/**
	 * double environment variables, both for 'dominant local' and 'recessive global' 
	 */
	@Test
	public void testOverrideMerge() {
		File local = new File( input, "dominant.settings.xml");
		File global = new File( input, "recessive.settings.xml");
		OverridingEnvironment ove = new OverridingEnvironment( StandardEnvironment.INSTANCE);
		ove.setEnv( MavenSettingsLoader.ENV_LOCAL_SETTINGS, local.getAbsolutePath()); //
		ove.setEnv( MavenSettingsLoader.ENV_GLOBAL_SETTINGS, global.getAbsolutePath()); //
		
		// 'null'-out all other env settings
		ove.setEnv(MavenSettingsLoader.ENV_EXCLUSIVE_SETTINGS, null); // 
		ove.setProperty( MavenSettingsLoader.USERHOME, "aint_user_home");
		ove.setEnv( MavenSettingsLoader.M2HOME, "aint_m2_home");
		
		
		MavenSettingsLoader loader = new MavenSettingsLoader();
		loader.setVirtualEnvironment(ove);
		
		MavenSettingsCompiler compiler = new MavenSettingsCompiler();
		compiler.setSettingsSupplier( loader);
		
		
		RepositoryConfiguration repositoryConfiguration = compiler.get();
								
		// check origination		
		Reason origination = repositoryConfiguration.getOrigination();
		
		List<String> declarations = ReasoningHelper.getDeclarations(origination);
		Assert.assertTrue( "expected [2] files, yet found [" + declarations.size() + "]", declarations.size() == 2);
		
		boolean matchDominant = false, matchRecessive = false;
		for (String declaration : declarations) {
			if (declaration.equals( local.getAbsolutePath())) {
				matchDominant = true;
			}
			if (declaration.equals( global.getAbsolutePath())) {
				matchRecessive = true;
			}
		}
		
		Assert.assertTrue( "File  [" + local.getAbsolutePath() + "] is not part of merge", matchDominant);
		Assert.assertTrue( "File  [" + global.getAbsolutePath() + "] is not part of merge", matchRecessive);
		
		List<String> pointers = ReasoningHelper.getPointers(origination);
		Assert.assertTrue( "expected [2] pointers, yet found [" + declarations.size() + "]", pointers.size() == 2);
		
		
		boolean pointerDominant = false, pointerRecessive = false;
		for (String pointer : pointers) {
			if (pointer.equals( MavenSettingsLoader.ENV_LOCAL_SETTINGS)) {
				pointerDominant = true;
			}
			if (pointer.equals( MavenSettingsLoader.ENV_GLOBAL_SETTINGS)) {
				pointerRecessive = true;
			}
		}
		
		Assert.assertTrue( "Variable  [" + MavenSettingsLoader.ENV_LOCAL_SETTINGS + "] is not part of merge", pointerDominant);
		Assert.assertTrue( "Variable  [" + MavenSettingsLoader.ENV_GLOBAL_SETTINGS + "] is not part of merge", pointerRecessive);
		
		System.out.println( origination.asFormattedText());	
	
	}
	
	/**
	 * maven standard : only the one in ${user.home}/.m2, named settings.xml 
	 */
	@Test
	public void testExclusiveUserHome() {
		OverridingEnvironment ove = new OverridingEnvironment( StandardEnvironment.INSTANCE);
		File userHome = new File( input, "initial/user.home");
		ove.setProperty( MavenSettingsLoader.USERHOME, userHome.getAbsolutePath());
		
		// 'null'-out all other env settings
		ove.setEnv(MavenSettingsLoader.ENV_LOCAL_SETTINGS, null); //
		ove.setEnv(MavenSettingsLoader.ENV_EXCLUSIVE_SETTINGS, null); // 		
		ove.setEnv(MavenSettingsLoader.ENV_GLOBAL_SETTINGS, null); //
		ove.setEnv( MavenSettingsLoader.M2HOME, "aint_m2_home");
		
		MavenSettingsLoader loader = new MavenSettingsLoader();
		loader.setVirtualEnvironment(ove);
		
		MavenSettingsCompiler compiler = new MavenSettingsCompiler();
		compiler.setSettingsSupplier( loader);
		
		
		RepositoryConfiguration repositoryConfiguration = compiler.get();
								
		// check origination		
		Reason origination = repositoryConfiguration.getOrigination();
		
		
	
		// declaration
		String found = ReasoningHelper.getDeclarationFile(origination);
		String expected = new File( userHome, ".m2/settings.xml").getAbsolutePath();		
		Assert.assertTrue("expected declaring file to be [" + expected + "], yet found [" + found, expected.equals(found));
		
		// pointer
		List<String> pointers = ReasoningHelper.getPointers(origination);
		Assert.assertTrue("expected only a single pointer, yet found [" + pointers.size() + "]", pointers.size() == 1);
		
		String pointer = pointers.get(0);
		String expectedPointer = "{" + MavenSettingsLoader.USERHOME + "}/.m2";
		Assert.assertTrue( "expected pointer to be [" + expectedPointer +"], yet found [" + pointer + "]", expectedPointer.equals(pointer));
						
		System.out.println( origination.stringify());				
		
		
	}
	/**
	 * maven standard: only the one in ${M2_HOME}/conf, named settings.xml
	 */
	@Test
	public void testExclusiveMavenHome() {
		OverridingEnvironment ove = new OverridingEnvironment( StandardEnvironment.INSTANCE);
		File mavenHome = new File( input, "initial/m2.home");
		ove.setEnv( MavenSettingsLoader.M2HOME, mavenHome.getAbsolutePath());
		
		// 'null'-out all other env settings
		ove.setEnv(MavenSettingsLoader.ENV_LOCAL_SETTINGS, null); //
		ove.setEnv(MavenSettingsLoader.ENV_EXCLUSIVE_SETTINGS, null); // 		
		ove.setEnv(MavenSettingsLoader.ENV_GLOBAL_SETTINGS, null); //
		ove.setProperty( MavenSettingsLoader.USERHOME, "aint_user_home");
		
		MavenSettingsLoader loader = new MavenSettingsLoader();
		loader.setVirtualEnvironment(ove);
		
		MavenSettingsCompiler compiler = new MavenSettingsCompiler();
		compiler.setSettingsSupplier( loader);
		
		
		RepositoryConfiguration repositoryConfiguration = compiler.get();
								
		// check origination		
		Reason origination = repositoryConfiguration.getOrigination();
	
		// declaration
		String found = ReasoningHelper.getDeclarationFile(origination);
		String expected = new File( mavenHome, "conf/settings.xml").getAbsolutePath();		
		Assert.assertTrue("expected declaring file to be [" + expected + "], yet found [" + found, expected.equals(found));
		
		// pointer
		List<String> pointers = ReasoningHelper.getPointers(origination);
		Assert.assertTrue("expected only a single pointer, yet found [" + pointers.size() + "]", pointers.size() == 1);
		
		String pointer = pointers.get(0);
		String expectedPointer = "{" + MavenSettingsLoader.M2HOME + "}/conf";
		Assert.assertTrue( "expected pointer to be [" + expectedPointer +"], yet found [" + pointer + "]", expectedPointer.equals(pointer));
						
		System.out.println( origination.stringify());		
		
	}
	/**
	 * maven standard: merge of ${user.home}/.m2/settings.xml, and ${M2_HOME}/conf/settings.xml 
	 */
	@Test
	public void testStandardMerge() {
		OverridingEnvironment ove = new OverridingEnvironment( StandardEnvironment.INSTANCE);
		
		File m2home = new File( input, "initial/m2.home");
		ove.setEnv( MavenSettingsLoader.M2HOME, m2home.getAbsolutePath());
	
		File userHome = new File( input, "initial/user.home");
		ove.setProperty( MavenSettingsLoader.USERHOME, userHome.getAbsolutePath());
		
		// 'null'-out all other env settings
		ove.setEnv(MavenSettingsLoader.ENV_LOCAL_SETTINGS, null); //
		ove.setEnv(MavenSettingsLoader.ENV_EXCLUSIVE_SETTINGS, null); // 		
		ove.setEnv(MavenSettingsLoader.ENV_GLOBAL_SETTINGS, null); //
		
		MavenSettingsLoader loader = new MavenSettingsLoader();
		loader.setVirtualEnvironment(ove);
		
		MavenSettingsCompiler compiler = new MavenSettingsCompiler();
		compiler.setSettingsSupplier( loader);
		
		
		RepositoryConfiguration repositoryConfiguration = compiler.get();
								
		// check origination		
		Reason origination = repositoryConfiguration.getOrigination();
		
		List<String> declarations = ReasoningHelper.getDeclarations(origination);
		Assert.assertTrue( "expected [2] files, yet found [" + declarations.size() + "]", declarations.size() == 2);
		
		File local = new File( userHome, ".m2/settings.xml");
		File global = new File( m2home, "conf/settings.xml");
		
		boolean matchDominant = false, matchRecessive = false;
		for (String declaration : declarations) {
			if (declaration.equals( local.getAbsolutePath())) {
				matchDominant = true;
			}
			if (declaration.equals( global.getAbsolutePath())) {
				matchRecessive = true;
			}
		}
		
		Assert.assertTrue( "File  [" + local.getAbsolutePath() + "] is not part of merge", matchDominant);
		Assert.assertTrue( "File  [" + global.getAbsolutePath() + "] is not part of merge", matchRecessive);
		
		List<String> pointers = ReasoningHelper.getPointers(origination);
		Assert.assertTrue( "expected [2] pointers, yet found [" + declarations.size() + "]", pointers.size() == 2);
		
		String dominantPath = "{user.home}/.m2";
		String recessivePath = "{M2_HOME}/conf";
		
		boolean pointerDominant = false, pointerRecessive = false;
		for (String pointer : pointers) {
			if (pointer.equals( dominantPath)) {
				pointerDominant = true;
			}
			if (pointer.equals( recessivePath)) {
				pointerRecessive = true;
			}
		}
		
		Assert.assertTrue( "Dominant path  [" + dominantPath + "] is not part of merge", pointerDominant);
		Assert.assertTrue( "Recessive path  [" + recessivePath + "] is not part of merge", pointerRecessive);
		
		System.out.println( origination.stringify());	
		
	}
}
