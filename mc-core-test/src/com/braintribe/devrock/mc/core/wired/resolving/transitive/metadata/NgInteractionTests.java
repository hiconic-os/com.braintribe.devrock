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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.metadata;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.repository.configuration.HasConnectivityTokens;
import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.RepoletUsingTrait;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.devrock.repolet.launcher.LauncherTrait;
import com.braintribe.devrock.repolet.launcher.builder.cfg.LauncherCfg;
import com.braintribe.devrock.repolet.launcher.builder.cfg.RepoletCfg;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.utils.lcd.LazyInitialized;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * tests that mc uses the correct files to persist its metadata, such as
 *  globally : 
 * - group-index-<repo>.txt
 * - last-changes-access-<repo>.yaml
 * - last-probing-result-<repo>.yaml
 *  per artifact 
 * - maven-metadata-<repo>.xml
 * - part-availability-<repo>.txt
 * 
 * tests run with RH updates, and blocked updates as being offline
 *  
 * @author pit
 *
 */
public class NgInteractionTests implements LauncherTrait, HasConnectivityTokens, HasCommonFilesystemNode {
	private static final String COM_BRAINTRIBE_DEVROCK_TEST = "com/braintribe/devrock/test";
	
	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/metadata");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	
	
	
	private File initial = new File( input, "initial");	
	private File settings = new File( input, "settings");

	
	private File oneCfg = new File( input, "content.definition.one.txt");
	private File twoCfg = new File( input, "content.definition.two.txt");
	
	private File oneValidationForA = new File( input, "content.validation.one.a.txt");
	private File twoValidationForA = new File( input, "content.validation.two.a.txt");
	
	private File oneValidationForX = new File( input, "content.validation.one.x.txt");
	private File twoValidationForX = new File( input, "content.validation.two.x.txt");

	private File currentSettings = new File( settings, "basic-settings.xml"); 
	protected TransitiveResolutionContext standardResolutionContext = TransitiveResolutionContext.build().done();
	
	private RepoletContent contentOne;
	{
		try {
			contentOne = RepositoryGenerations.parseConfigurationFile(oneCfg);		
		} catch (Exception e) {
			Assert.fail("cannot process first content");
			throw new IllegalStateException();
		}
	}
	
	private RepoletContent contentTwo;
	{
		try {
			contentTwo = RepositoryGenerations.parseConfigurationFile(twoCfg);		
		} catch (Exception e) {
			Assert.fail("cannot process second content");
		}
		
	}
	
	private Launcher launcher;	
	{
		launcher = Launcher.build()
				.repolet()
					.name("archive")
					.indexedDescriptiveContent()
						.initialIndex("one")
						.descriptiveContent("one", contentOne)
						.descriptiveContent( "two", contentTwo)
					.close()
					.changesUrl("http://localhost:${port}/archive/rest/changes")
				.close()
			.done();				
	}
	
	private class Param {
		public String stage;
		public boolean activate;
		public String terminal;
		public File validation;
		public List<Pair<File,Boolean>> filesToCheckForExistance = new ArrayList<>();
		public String name;
		
		public Param(String stage, boolean activate, String terminal, File validation) {
			this.stage = stage;
			this.activate = activate;
			this.terminal = terminal;			
			this.validation = validation;
		 
		}
		
	}
	
	private List<Pair<File,Boolean>> defaultFilesToExist = new ArrayList<>();
	{
		defaultFilesToExist.add( Pair.of(new File( repo, "group-index-archive.txt"), true));
		defaultFilesToExist.add( Pair.of(new File( repo, "last-changes-access-archive.yaml"), true));	
	}
	
	private List<Pair<File,Boolean>> mavenMetaDataFilesToExistForAInStageOne = new ArrayList<>();
	{
		mavenMetaDataFilesToExistForAInStageOne.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/a/maven-metadata-archive.xml"), true));
		mavenMetaDataFilesToExistForAInStageOne.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/b/maven-metadata-archive.xml"), true));
		mavenMetaDataFilesToExistForAInStageOne.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/c/maven-metadata-archive.xml"), true));
		
		mavenMetaDataFilesToExistForAInStageOne.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/a/1.0.1/part-availability-archive.txt"), true));
		mavenMetaDataFilesToExistForAInStageOne.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/b/1.0.1/part-availability-archive.txt"), true));
		mavenMetaDataFilesToExistForAInStageOne.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/b/1.0.1/part-availability-archive.txt"), true));
		
		mavenMetaDataFilesToExistForAInStageOne.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/a/1.0.2/part-availability-archive.txt"), false));
		mavenMetaDataFilesToExistForAInStageOne.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/b/1.0.2/part-availability-archive.txt"), false));
		mavenMetaDataFilesToExistForAInStageOne.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/b/1.0.2/part-availability-archive.txt"), false));
				
	}
	
	private List<Pair<File,Boolean>> mavenMetaDataFilesToExistForAInStageTwo = new ArrayList<>();
	{
		mavenMetaDataFilesToExistForAInStageTwo.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/a/maven-metadata-archive.xml"), true));
		mavenMetaDataFilesToExistForAInStageTwo.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/b/maven-metadata-archive.xml"), true));
		mavenMetaDataFilesToExistForAInStageTwo.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/c/maven-metadata-archive.xml"), true));
		
		mavenMetaDataFilesToExistForAInStageTwo.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/a/1.0.1/part-availability-archive.txt"), true));
		mavenMetaDataFilesToExistForAInStageTwo.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/b/1.0.1/part-availability-archive.txt"), true));
		mavenMetaDataFilesToExistForAInStageTwo.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/b/1.0.1/part-availability-archive.txt"), true));
		
		mavenMetaDataFilesToExistForAInStageTwo.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/a/1.0.2/part-availability-archive.txt"), true));
		mavenMetaDataFilesToExistForAInStageTwo.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/b/1.0.2/part-availability-archive.txt"), true));
		mavenMetaDataFilesToExistForAInStageTwo.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/b/1.0.2/part-availability-archive.txt"), true));
				
	}
	
	
	
	
	private List<Pair<File,Boolean>> markerForMavenMetaDataFilesToExistForA = new ArrayList<>();
	{		
		markerForMavenMetaDataFilesToExistForA.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/x/maven-metadata-archive.xml.outdated"), true));		
		markerForMavenMetaDataFilesToExistForA.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/y/maven-metadata-archive.xml.outdated"), true));		
		markerForMavenMetaDataFilesToExistForA.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/z/maven-metadata-archive.xml.outdated"), true));
	}
	
	
	private List<Pair<File,Boolean>> mavenMetaDataFilesToExistForX = new ArrayList<>();
	{
		mavenMetaDataFilesToExistForX.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/x/maven-metadata-archive.xml"), true));
		mavenMetaDataFilesToExistForX.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/y/maven-metadata-archive.xml"), true));
		mavenMetaDataFilesToExistForX.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/z/maven-metadata-archive.xml"), true));
		
		mavenMetaDataFilesToExistForX.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/x/1.0.1/part-availability-archive.txt"), true));
		mavenMetaDataFilesToExistForX.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/y/1.0.1/part-availability-archive.txt"), true));
		mavenMetaDataFilesToExistForX.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/z/1.0.1/part-availability-archive.txt"), true));
	}
	
	private List<Pair<File,Boolean>> markerForMavenMetaDataFilesToExistForXOffline = new ArrayList<>();
	{		
		markerForMavenMetaDataFilesToExistForXOffline.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/x/maven-metadata-archive.xml.outdated"), true));		
		markerForMavenMetaDataFilesToExistForXOffline.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/y/maven-metadata-archive.xml.outdated"), true));		
		markerForMavenMetaDataFilesToExistForXOffline.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/z/maven-metadata-archive.xml.outdated"), true));
	}
	
	private List<Pair<File,Boolean>> markerForMavenMetaDataFilesToExistForXOnline = new ArrayList<>();
	{		
		markerForMavenMetaDataFilesToExistForXOnline.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/x/maven-metadata-archive.xml.outdated"), false));		
		markerForMavenMetaDataFilesToExistForXOnline.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/y/maven-metadata-archive.xml.outdated"), false));		
		markerForMavenMetaDataFilesToExistForXOnline.add( Pair.of( new File( repo, COM_BRAINTRIBE_DEVROCK_TEST + "/z/maven-metadata-archive.xml.outdated"), false));
	}



	private List<Param> stages = new ArrayList<>();
	{
		// initial runs on a and x
		Param param = new Param( null, true, "com.braintribe.devrock.test:t#1.0.1", oneValidationForA);
		param.filesToCheckForExistance.addAll( defaultFilesToExist);
		param.filesToCheckForExistance.addAll( mavenMetaDataFilesToExistForAInStageOne);
		param.name ="first run on A";
		
		stages.add( param);
		
		
		param = new Param( null, true, "com.braintribe.devrock.test:t#1.0.2",  oneValidationForX);
		stages.add( param);
		param.filesToCheckForExistance.addAll( defaultFilesToExist);
		param.filesToCheckForExistance.addAll( mavenMetaDataFilesToExistForX);
		param.name ="first run on X";
	
		
		// switch state and run on a
		param = new Param( "two", true, "com.braintribe.devrock.test:t#1.0.1",  twoValidationForA);
		stages.add( param);
		param.name ="second run on A, stage two";
		
		param.filesToCheckForExistance.addAll( defaultFilesToExist);
		param.filesToCheckForExistance.addAll( mavenMetaDataFilesToExistForAInStageTwo);
		param.filesToCheckForExistance.addAll( markerForMavenMetaDataFilesToExistForA);
		
		
		
		// deactivate repolet, run on X
		param = new Param( null, false,  "com.braintribe.devrock.test:t#1.0.2", oneValidationForX);
		stages.add( param);
		param.filesToCheckForExistance.addAll( defaultFilesToExist);
		param.filesToCheckForExistance.addAll( mavenMetaDataFilesToExistForAInStageTwo);
		param.filesToCheckForExistance.addAll( mavenMetaDataFilesToExistForX);
		param.filesToCheckForExistance.addAll( markerForMavenMetaDataFilesToExistForXOffline);
		param.name = "second run on X on inaccessible archive";
	
		// activate repolet, run on X again
		param = new Param( null, true, "com.braintribe.devrock.test:t#1.0.2", twoValidationForX);
		stages.add( param);
		param.filesToCheckForExistance.addAll( defaultFilesToExist);
		param.filesToCheckForExistance.addAll( mavenMetaDataFilesToExistForX);
		param.filesToCheckForExistance.addAll( markerForMavenMetaDataFilesToExistForXOnline);
		param.name = "third run on X on accessible archive";
	
		
	}
	
	protected LazyInitialized<CloseableHttpClient> httpClient = new LazyInitialized<>( RepoletUsingTrait::client);
	
	@Before
	public void runBefore() {
		TestUtils.ensure(output);
		repo.mkdirs();
		if (initial.exists()) {
			TestUtils.copy(initial, repo);
		}
		
		launcher.launch();
	}
	
	@After
	public void runAfter() {
		if (launcher.isRunning()) {
			launcher.shutdown();
		}
	}
	
	
	@Test
	public void runTest() {		
		for (Param param : stages) {
			String stage = param.stage;
			if (stage != null) {
				System.out.println("switching repolet to stage [" + stage + "]");
				if (!launcher.isRunning()) {
					launcher.launch();
				}
				setupStage( param);				
			}			
			try {
				//  				
				AnalysisArtifactResolution resolution = run( param.terminal, standardResolutionContext, param);				
				validate( resolution, param);						
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail("exception thrown " + e.getLocalizedMessage() + "]");
			}
		}
	}
	
	protected OverridingEnvironment buildVirtualEnvironement(Map<String,String> overrides) {
		OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		if (overrides != null && !overrides.isEmpty()) {
			ove.setEnvs(overrides);						
		}
		ove.setEnv("repo", repo.getAbsolutePath());
		ove.setEnv("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", currentSettings.getAbsolutePath());
		ove.setEnv( "port", Integer.toString( launcher.getAssignedPort()));
				
		return ove;		
	}

	protected AnalysisArtifactResolution run(String terminal, TransitiveResolutionContext resolutionContext, Param param) {
		final Map<String,String> overrides = new HashMap<>();
		if (!param.activate) {							
			overrides.put(MC_CONNECTIVITY_MODE, MODE_OFFLINE);
		}
		
		try (				
				WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
					.bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement( overrides))				
					.build();
			) {
			
			TransitiveDependencyResolver transitiveDependencyResolver = resolverContext.contract().transitiveDependencyResolver();
			
			CompiledTerminal cdi = CompiledTerminal.from ( CompiledDependencyIdentification.parse( terminal));
			AnalysisArtifactResolution artifactResolution = transitiveDependencyResolver.resolve( resolutionContext, cdi);
			return artifactResolution;					
								
		}
		catch( Exception e) {
			e.printStackTrace();
			Assert.fail("exception thrown [" + e.getLocalizedMessage() + "]");		
		}
		return null;
	}
	
		

	private void setupStage(Param param) {
		LauncherCfg cfg = launcher.getLaunchedCfg();
		for (RepoletCfg rcfg : cfg.getRepoletCfgs()) {			
			String url = "http://localhost:${port}/" + rcfg.getName() + "/update";
			url = url.replace("${port}", "" + cfg.getPort());			
			try {
				CloseableHttpResponse response = RepoletUsingTrait.retrieveGetResponse( httpClient.get(), url + "?key=" + param.stage);
				HttpEntity entity = response.getEntity();
				EntityUtils.consume(entity);
			} catch (IOException e) {	
				e.printStackTrace();
				Assert.fail("exception thrown while switching :" + e.getMessage());
			}
		}
		
	}
	
	
	private void validate(AnalysisArtifactResolution resolution, Param param) {		
		if (param.validation == null) {
			return;
		}
		Validator validator = new Validator();
		validator.validateExpressive( param.validation, resolution);
		validator.assertResults();
		// validate persistence files
		if (param.filesToCheckForExistance !=  null) {
			for (Pair<File,Boolean> pair : param.filesToCheckForExistance) {
				File file = pair.first;
				boolean mustExist = pair.second;
				if (mustExist) {
					Assert.assertTrue( param.name + ": file [" + file + "] doesn't exist", file.exists());
				}
				else {
					Assert.assertTrue(param.name + ": file [" + file + "] does exist", !file.exists());
				}				
			}
		}
	}

}
