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
package com.braintribe.devrock.mc.core.compiler.configuration.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.core.compiler.AbstractCompilerTest;
import com.braintribe.devrock.mc.core.compiler.RepositoryConfigurationValidator;
import com.braintribe.devrock.mc.core.configuration.maven.MavenSettingsCompiler;
import com.braintribe.devrock.model.repository.ChecksumPolicy;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.marshaller.artifact.maven.settings.DeclaredMavenSettingsMarshaller;
import com.braintribe.model.artifact.maven.settings.Settings;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

/**
 * tests the multiple supported ways to active a profile in a settings.xml. Tests are simple : depending on the switches, 
 * the resulting repositories with the repository configuration are different  
 * @author pit
 *
 */
public class RepositoryConfigurationMavenProfileActivationTest extends AbstractCompilerTest {

	private DeclaredMavenSettingsMarshaller marshaller = new DeclaredMavenSettingsMarshaller();
	
	private TimeSpan daily;
	{
		daily = TimeSpan.T.create();
		daily.setUnit( TimeUnit.day);
		daily.setValue( 1);
	}


	@Override
	protected String getRoot() {
		return "compiler/maven.settings/activation";
	}
	
	Settings loadSettings( File file) {
		try (InputStream in = new FileInputStream( file)){			
			Settings settings = marshaller.unmarshall(in);		
			return settings;
		} catch (Exception e) {
			throw new IllegalStateException("cannot read file [" + file.getAbsolutePath() + "]", e);
		}
	}
	
	private void test(File file, RepositoryConfiguration expected, VirtualEnvironment ve) {
		Settings settings = loadSettings( file);
		MavenSettingsCompiler compiler = new MavenSettingsCompiler();
		compiler.setSettingsSupplier( () -> settings);
		compiler.setVirtualEnvironment( ve);
		RepositoryConfiguration repositoryConfiguration = compiler.get();
		if (expected != null) {
			RepositoryConfigurationValidator.validate( expected, repositoryConfiguration);
		
		}		
	}
	
	private RepositoryConfiguration generateTestRepositoryConfiguration( @SuppressWarnings("unchecked") Pair<String,String> ... pairs) {
		RepositoryConfiguration rc = RepositoryConfiguration.T.create();
		rc.setLocalRepositoryPath( "myRepo");
		
		for (Pair<String,String> pair : pairs) {
			MavenHttpRepository repo = MavenHttpRepository.T.create();
			repo.setName( pair.first);
			repo.setUrl( pair.second);
			repo.setSnapshotRepo( false);
			repo.setUpdateTimeSpan(daily);
			repo.setCheckSumPolicy(ChecksumPolicy.ignore);
			
			rc.getRepositories().add(repo);
		}
		
		return rc;
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testJdkSwitch() {
		File jdkProfileSettings = new File( input, "jdk.settings.xml");
		OverridingEnvironment ove = new OverridingEnvironment( StandardEnvironment.INSTANCE);
		ove.setProperty("java.specification.version", "9");
		
		RepositoryConfiguration expectedForJdk_9 = generateTestRepositoryConfiguration( Pair.of( "jdk-9", "http://jdk-9"));
		test( jdkProfileSettings, expectedForJdk_9, ove);
		
		ove.setProperty("java.specification.version", "1.4");		
		RepositoryConfiguration expectedForJdk_1_4 = generateTestRepositoryConfiguration( Pair.of( "jdk-8", "http://jdk-8"));
		test( jdkProfileSettings, expectedForJdk_1_4, ove);
		
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testOsSwitch() {	
		File jdkProfileSettings = new File( input, "os.settings.xml");
		OverridingEnvironment ove = new OverridingEnvironment( StandardEnvironment.INSTANCE);
		ove.setProperty("os.name", "Windows 10");
		ove.setProperty("os.arch", "amd64");
		ove.setProperty("os.version", "10");
		
		RepositoryConfiguration expectedForWin_10_amd = generateTestRepositoryConfiguration( Pair.of( "win-10", "http://win-10"));
		test( jdkProfileSettings, expectedForWin_10_amd, ove);


		ove.setProperty("os.name", "Windows 10");
		ove.setProperty("os.arch", "x64");
		ove.setProperty("os.version", "10");

		RepositoryConfiguration expectedForWin_10_x64 = generateTestRepositoryConfiguration(Pair.of( "win-10x64", "http://win-10x64"));
		test( jdkProfileSettings, expectedForWin_10_x64, ove);
		
		ove.setProperty("os.name", "Windows XP");
		ove.setProperty("os.arch", "x86");
		ove.setProperty("os.version", "5.1.2600");

		RepositoryConfiguration expectedForWin_XP = generateTestRepositoryConfiguration( Pair.of( "win-XP", "http://win-xp"));
		test( jdkProfileSettings, expectedForWin_XP, ove);		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testEnvironmentVariableSwitch() {	
		File environmentProfileSettings = new File( input, "env.settings.xml");
		// 
		OverridingEnvironment ove = new OverridingEnvironment( StandardEnvironment.INSTANCE);

		// a) no variable
		RepositoryConfiguration expectedForNoVar = generateTestRepositoryConfiguration(Pair.of( "no-var", "http://no-var"));
		test( environmentProfileSettings, expectedForNoVar, ove);
		
		
		// b) variable set 
		// a) no variable
	
		RepositoryConfiguration expectedForSetVar = generateTestRepositoryConfiguration(
																						Pair.of( "no-match-var", "http://no-match-var"),
																						Pair.of( "var", "http://var")
																						);
		ove.setEnv("variable", "beurk");
		test( environmentProfileSettings, expectedForSetVar, ove);
	
		RepositoryConfiguration expectedForMatchingVar = generateTestRepositoryConfiguration(
				Pair.of( "match-var", "http://match-var"),
				Pair.of( "var", "http://var")
				);
			ove.setEnv("variable", "match");
		test( environmentProfileSettings, expectedForMatchingVar, ove);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testPropertySwitch() {	
		File existingFileProfileSettings = new File( input, "property.settings.xml");
		// 
		OverridingEnvironment ove = new OverridingEnvironment( StandardEnvironment.INSTANCE);

		// a) no variable
		RepositoryConfiguration expectedForNoVar = generateTestRepositoryConfiguration(Pair.of( "no-property", "http://no-property"));
		test( existingFileProfileSettings, expectedForNoVar, ove);
		
		
		// b) variable set 
		// a) no variable
	
		RepositoryConfiguration expectedForSetVar = generateTestRepositoryConfiguration(
																						Pair.of( "no-match-property", "http://no-match-property"),
																						Pair.of( "property", "http://property")
																						);
		ove.setProperty("property", "beurk");
		test( existingFileProfileSettings, expectedForSetVar, ove);
	
		RepositoryConfiguration expectedForMatchingVar = generateTestRepositoryConfiguration(
				Pair.of( "match-property", "http://match-property"),
				Pair.of( "property", "http://property")
				);
			ove.setProperty("property", "match");
		test( existingFileProfileSettings, expectedForMatchingVar, ove);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testFileSwitch() {	
		File fileProfileSettings = new File( input, "file.settings.xml");
	
		OverridingEnvironment ove = new OverridingEnvironment( StandardEnvironment.INSTANCE);
		String existingFilePath = new File( input, "existing.file.marker.txt").getAbsolutePath();
		ove.setEnv("existing", existingFilePath);
		ove.setEnv("missing", existingFilePath);
		
		RepositoryConfiguration expectedForExistingFile = generateTestRepositoryConfiguration( Pair.of( "file", "http://file"));
		test( fileProfileSettings, expectedForExistingFile, ove);	
		
		
		String missingFilePath = new File( input, "missing.file.marker.txt").getAbsolutePath();
		ove.setEnv("existing", missingFilePath);
		ove.setEnv("missing", missingFilePath);
		
		RepositoryConfiguration expectedForMissingFile = generateTestRepositoryConfiguration( Pair.of( "no-file", "http://no-file"));
		test( fileProfileSettings, expectedForMissingFile, ove);	
				
	}
	
	
	
	public static void main(String[] args) {
		System.out.println("os name : " + System.getProperty("os.name"));
		System.out.println("os arch : " + System.getProperty("os.arch"));
		System.out.println("os version : " + System.getProperty("os.version"));
	}
}
