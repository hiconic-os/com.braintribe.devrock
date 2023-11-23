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

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.compiler.RepositoryConfigurationValidator;
import com.braintribe.devrock.mc.core.configuration.maven.MavenSettingsCompiler;
import com.braintribe.devrock.model.repository.ChecksumPolicy;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.model.repository.filters.QualifiedArtifactFilter;
import com.braintribe.marshaller.artifact.maven.settings.DeclaredMavenSettingsMarshaller;
import com.braintribe.model.artifact.maven.settings.Settings;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;

/**
 * tests multi-stage merging of repository configuration  
 *  
 * @author pit
 *
 */
public class RepositoryConfigurationFilterMergingTest implements HasCommonFilesystemNode {
	private DeclaredMavenSettingsMarshaller marshaller = new DeclaredMavenSettingsMarshaller();
	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("compiler/maven.settings/merging");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	
	private TimeSpan always;
	{
		always = TimeSpan.T.create();
		always.setValue(0);		
	}
	
	protected RepositoryConfiguration basicConfiguration;
	{
		basicConfiguration = RepositoryConfiguration.T.create();
		basicConfiguration.setLocalRepositoryPath( "myDrive:/.m2/repository-groups/");
		
		MavenHttpRepository iDeclaredRelease = MavenHttpRepository.T.create();
		iDeclaredRelease.setName( "declared_repo");
		iDeclaredRelease.setUrl( "http://declared_url");
		iDeclaredRelease.setUpdateTimeSpan( null);
		iDeclaredRelease.setSnapshotRepo(false);		
		iDeclaredRelease.setUser("declared_user");
		iDeclaredRelease.setPassword("declared_password");
		iDeclaredRelease.setCheckSumPolicy( ChecksumPolicy.ignore);
		basicConfiguration.getRepositories().add(iDeclaredRelease);
		
		MavenHttpRepository iDeclaredSnapshot = MavenHttpRepository.T.create();
		iDeclaredSnapshot.setName( "declared_repo");
		iDeclaredSnapshot.setUrl( "http://declared_url");
		iDeclaredSnapshot.setUser("declared_user");
		iDeclaredSnapshot.setPassword("declared_password");
		iDeclaredSnapshot.setUpdateTimeSpan( always);
		iDeclaredSnapshot.setSnapshotRepo(true);		
		iDeclaredSnapshot.setCheckSumPolicy( ChecksumPolicy.ignore);
		basicConfiguration.getRepositories().add(iDeclaredSnapshot);
				
	}
	
	protected RepositoryConfiguration injectedConfiguration;
	{
		injectedConfiguration = RepositoryConfiguration.T.create();
		injectedConfiguration.setLocalRepositoryPath( "myDrive:/.m2/repository-groups/");
		
		MavenHttpRepository iDeclaredRelease2 = MavenHttpRepository.T.create();
		iDeclaredRelease2.setName( "declared_repo");
		iDeclaredRelease2.setUrl( "http://declared_url");
		iDeclaredRelease2.setUpdateTimeSpan( null);
		iDeclaredRelease2.setSnapshotRepo(false);		
		iDeclaredRelease2.setUser("declared_user");
		iDeclaredRelease2.setPassword("declared_password");
		iDeclaredRelease2.setCheckSumPolicy( ChecksumPolicy.ignore);
		
		// add filter
		QualifiedArtifactFilter declaredFilter = QualifiedArtifactFilter.T.create();
		declaredFilter.setGroupId("com.braintribe.devrock.test");
		declaredFilter.setArtifactId("t");
		iDeclaredRelease2.setArtifactFilter(declaredFilter);
		
		injectedConfiguration.getRepositories().add(iDeclaredRelease2);
		
		MavenHttpRepository iDeclaredSnapshot2 = MavenHttpRepository.T.create();
		iDeclaredSnapshot2.setName( "declared_repo");
		iDeclaredSnapshot2.setUrl( "http://declared_url");
		iDeclaredSnapshot2.setUser("declared_user");
		iDeclaredSnapshot2.setPassword("declared_password");
		iDeclaredSnapshot2.setUpdateTimeSpan( always);
		iDeclaredSnapshot2.setSnapshotRepo(true);		
		iDeclaredSnapshot2.setCheckSumPolicy( ChecksumPolicy.ignore);
		// add filter
		iDeclaredSnapshot2.setArtifactFilter(declaredFilter);
		
		injectedConfiguration.getRepositories().add(iDeclaredSnapshot2);
		
		MavenHttpRepository added_repo = MavenHttpRepository.T.create();
		added_repo.setName( "added_repo");
		added_repo.setUrl( "http://added_url");
		added_repo.setUpdateTimeSpan( null);
		added_repo.setSnapshotRepo(false);		
		added_repo.setUser("added_user");
		added_repo.setPassword("added_password");
		added_repo.setCheckSumPolicy( ChecksumPolicy.ignore);
		
		// add filter						
		QualifiedArtifactFilter addedFilter = QualifiedArtifactFilter.T.create();
		addedFilter.setGroupId("com.braintribe.devrock.test");
		addedFilter.setArtifactId("a");
		added_repo.setArtifactFilter(addedFilter);
		
		injectedConfiguration.getRepositories().add( added_repo);
	}
	
	protected RepositoryConfiguration externalConfiguration;
	{
		externalConfiguration = RepositoryConfiguration.T.create();
		externalConfiguration.setLocalRepositoryPath( "myDrive:/.m2/repository-groups/");
		
		MavenHttpRepository iDeclaredRelease_3 = MavenHttpRepository.T.create();
		iDeclaredRelease_3.setName( "declared_repo");
		iDeclaredRelease_3.setUrl( "http://declared_url");
		iDeclaredRelease_3.setUpdateTimeSpan( null);
		iDeclaredRelease_3.setSnapshotRepo(false);		
		iDeclaredRelease_3.setUser("declared_user");
		iDeclaredRelease_3.setPassword("declared_password");
		iDeclaredRelease_3.setCheckSumPolicy( ChecksumPolicy.ignore);
		// add filter 
		QualifiedArtifactFilter declaredFilter = QualifiedArtifactFilter.T.create();
		declaredFilter.setGroupId("com.braintribe.devrock.test");
		declaredFilter.setArtifactId("t");
		iDeclaredRelease_3.setArtifactFilter(declaredFilter);
		
		externalConfiguration.getRepositories().add(iDeclaredRelease_3);
		
		MavenHttpRepository iDeclaredSnapshot_3 = MavenHttpRepository.T.create();
		iDeclaredSnapshot_3.setName( "declared_repo");
		iDeclaredSnapshot_3.setUrl( "http://declared_url");
		iDeclaredSnapshot_3.setUser("declared_user");
		iDeclaredSnapshot_3.setPassword("declared_password");
		iDeclaredSnapshot_3.setUpdateTimeSpan( always);
		iDeclaredSnapshot_3.setSnapshotRepo(true);		
		iDeclaredSnapshot_3.setCheckSumPolicy( ChecksumPolicy.ignore);
		// add filter
		iDeclaredSnapshot_3.setArtifactFilter(declaredFilter);
		externalConfiguration.getRepositories().add(iDeclaredSnapshot_3);
		
		MavenHttpRepository added_repo = MavenHttpRepository.T.create();
		added_repo.setName( "added_repo");
		added_repo.setUrl( "http://added_url");
		added_repo.setUpdateTimeSpan( null);
		added_repo.setSnapshotRepo(false);		
		added_repo.setUser("added_user");
		added_repo.setPassword("added_password");
		added_repo.setCheckSumPolicy( ChecksumPolicy.ignore);	
		// add filter						
		QualifiedArtifactFilter addedFilter = QualifiedArtifactFilter.T.create();
		addedFilter.setGroupId("com.braintribe.devrock.test");
		addedFilter.setArtifactId("a");
		added_repo.setArtifactFilter(addedFilter);
		
		externalConfiguration.getRepositories().add( added_repo);
		
		// 
		MavenHttpRepository added_repo_ex = MavenHttpRepository.T.create();
		added_repo_ex.setName( "added_repo_ex_1");
		added_repo_ex.setUrl( "http://added_ex1_url");
		added_repo_ex.setUpdateTimeSpan( null);
		added_repo_ex.setSnapshotRepo(false);		
		added_repo_ex.setUser("added_ex1_user");
		added_repo_ex.setPassword("added_ex1_password");
		added_repo_ex.setCheckSumPolicy( ChecksumPolicy.ignore);
		
		externalConfiguration.getRepositories().add( added_repo_ex);
		
		// add filter						
		QualifiedArtifactFilter externalFilter = QualifiedArtifactFilter.T.create();
		externalFilter.setGroupId("com.braintribe.devrock.test");
		externalFilter.setArtifactId("x");
		added_repo_ex.setArtifactFilter(externalFilter);
		
	}
	
	private Settings loadSettings( File file) {
		try (InputStream in = new FileInputStream( file)){			
			Settings settings = marshaller.unmarshall(in);		
			return settings;
		} catch (Exception e) {
			throw new IllegalStateException("cannot read file [" + file.getAbsolutePath() + "]", e);
		}
	}
	
	private void test(File settingsFile, File filterFile, RepositoryConfiguration expected) {
		Settings settings = loadSettings( settingsFile);
		MavenSettingsCompiler compiler = new MavenSettingsCompiler();
		compiler.setSettingsSupplier( () -> settings);
		
		if (filterFile != null) {
			OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
			ove.setEnv( MavenSettingsCompiler.DEVROCK_REPOSITORY_CONFIGURATION, filterFile.getAbsolutePath());
			compiler.setVirtualEnvironment(ove);
		}
		
		RepositoryConfiguration repositoryConfiguration = compiler.get();
		
		if (expected != null) {
			RepositoryConfigurationValidator.validate( expected, repositoryConfiguration);		
		}		
	}
	
	/**
	 * tests two error situations, both should throw an exception 
	 * @param settingsFile
	 * @param filterFile
	 */
	
	private void errorHandlingTest(File settingsFile, File filterFile) {
		Settings settings = loadSettings( settingsFile);
		MavenSettingsCompiler compiler = new MavenSettingsCompiler();
		compiler.setSettingsSupplier( () -> settings);
		
		if (filterFile != null) {
			OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
			ove.setEnv( MavenSettingsCompiler.DEVROCK_REPOSITORY_CONFIGURATION, filterFile.getAbsolutePath());
			compiler.setVirtualEnvironment(ove);
		}
		boolean exceptionThrown = false;
		
		try {
			@SuppressWarnings("unused")
			RepositoryConfiguration repositoryConfiguration = compiler.get();
		} catch (Exception e) {
			exceptionThrown = true;
		}
		
		Assert.assertTrue( "unexpectedly no exception is thrown", exceptionThrown);
	}
	
	@Test
	public void testNonExistingExternalFile() {
		errorHandlingTest( new File( input, "simple.settings.xml"), new File( input, "does-not-exist"));
	}
	
	@Test
	public void testCorruptedExternalFile() {
		errorHandlingTest( new File( input, "simple.settings.xml"), new File( input, "corrupted.no.yaml"));		
	}
	
	@Test
	public void runSettingsOnlyTest() {
		test( new File( input, "simple.settings.xml"), null, basicConfiguration);
	}	
	
	@Test
	public void runSettingsAndInjectedTest() {
		test( new File( input, "simple.settings.yaml.injection.xml"), null, injectedConfiguration);
	}
	
	@Test
	public void runSettingsAndInjectedAndExternalTest() {
		test( new File( input, "simple.settings.yaml.injection.xml"), new File( input, "external.yaml"), externalConfiguration);
	}
	
}

