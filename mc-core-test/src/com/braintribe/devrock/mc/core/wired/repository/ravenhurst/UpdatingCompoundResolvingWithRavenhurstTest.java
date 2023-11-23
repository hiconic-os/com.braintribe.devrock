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
package com.braintribe.devrock.mc.core.wired.repository.ravenhurst;




import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.commons.ArtifactAddressBuilder;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.ArtifactPartResolver;
import com.braintribe.devrock.mc.api.resolver.CompiledArtifactResolver;
import com.braintribe.devrock.mc.api.resolver.DependencyResolver;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.RepoletUsingTrait;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.resolver.ArtifactDataResolverModule;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.devrock.repolet.launcher.LauncherTrait;
import com.braintribe.devrock.repolet.launcher.builder.cfg.LauncherCfg;
import com.braintribe.devrock.repolet.launcher.builder.cfg.RepoletCfg;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.lcd.LazyInitialized;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;



/**
 * 
 * @author pit
 *
 */
@Category( KnownIssue.class)
public class UpdatingCompoundResolvingWithRavenhurstTest implements LauncherTrait, RepoletUsingTrait, HasCommonFilesystemNode {
	private static Logger log = Logger.getLogger(UpdatingCompoundResolvingWithRavenhurstTest.class);
	
	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots( getRoot());
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	
	private File resolverRepositoryA1 = new File( input, "remoteRepoA.1");
	private File resolverRepositoryA2 = new File( input, "remoteRepoA.2");
	private File resolverRepositoryB1 = new File( input, "remoteRepoB.1");
	private File resolverRepositoryB2 = new File( input, "remoteRepoB.2");
	
	private File preparedInitialRepository = new File( input, "initial");
	private File localRepository = new File( output, "repo");
	private File settings = new File( input, "settings/basic-settings.xml");

	private static String grp = "com.braintribe.devrock.test";
	private static String art = "artifact";
	private static String artA = "artifactA";
	private static String artB = "artifactB";
	private static String artC = "artifactC";
	private static String artD = "artifactD";
	
	protected String getRoot() {
		return "wired/ravenhurst.updating";
	}
	
	private Launcher launcher = Launcher.build()
					.repolet()
						.name("archiveA")
						.changesUrl("http://localhost:${port}/archiveA/rest/changes")
						.serverIdentification("repolet1")
						.indexedFilesystem()			
							.initialIndex("one")
							.filesystem("one", resolverRepositoryA1)
							.filesystem("two", resolverRepositoryA2)
						.close()
					.close()
					.repolet()
						.name("archiveB")
						.changesUrl("http://localhost:${port}/archiveB/rest/changes")
						.serverIdentification("repolet2")
						.indexedFilesystem()	
							.initialIndex("one")
							.filesystem("one", resolverRepositoryB1)
							.filesystem("two", resolverRepositoryB2)
						.close()
					.close()
				.done();	
	
	
	private List<CompiledArtifactIdentification> cais_artifact;
	{
		cais_artifact = new ArrayList<>();
		cais_artifact.add( CompiledArtifactIdentification.parse(grp + ":" + art + "#1.0"));
		cais_artifact.add( CompiledArtifactIdentification.parse(grp + ":" + art + "#2.0"));
		cais_artifact.add( CompiledArtifactIdentification.parse(grp + ":" + art + "#3.0"));
	}
	
	// pair: first - stage, second - pair: first - dependency as string, second - expected CAI 
	List<Pair<String, Pair<String, CompiledArtifactIdentification>>> dependencyExpectations = new ArrayList<>();
	{
		// first stage dependencies
		dependencyExpectations.add( Pair.of( "one", Pair.of( grp + ":" + art + "#[1.0,3.0]", CompiledArtifactIdentification.parse(grp + ":" + art + "#3.0"))));
		
		dependencyExpectations.add( Pair.of( "one", Pair.of( grp + ":" + artA + "#[1.0,1.1)", CompiledArtifactIdentification.parse(grp + ":" + artA + "#1.0.2-pc"))));
		dependencyExpectations.add( Pair.of( "one", Pair.of( grp + ":" + artB + "#[1.0,1.1)", CompiledArtifactIdentification.parse(grp + ":" + artB + "#1.0.2-pc"))));
		
		dependencyExpectations.add( Pair.of( "one", Pair.of( grp + ":" + artC + "#[1.0,1.1)", CompiledArtifactIdentification.parse(grp + ":" + artC + "#1.0.2-pc"))));
		dependencyExpectations.add( Pair.of( "one", Pair.of( grp + ":" + artD + "#[1.0,1.1)", CompiledArtifactIdentification.parse(grp + ":" + artD + "#1.0.2-pc"))));
		

		// second stage dependencies
		dependencyExpectations.add( Pair.of( "two", Pair.of( grp + ":" + art + "#[1.0,3.0]", CompiledArtifactIdentification.parse(grp + ":" + art + "#3.0"))));		
		
		dependencyExpectations.add( Pair.of( "two", Pair.of( grp + ":" + artA + "#[1.0,1.1)", CompiledArtifactIdentification.parse(grp + ":" + artA + "#1.0.3-pc"))));
		dependencyExpectations.add( Pair.of( "two", Pair.of( grp + ":" + artB + "#[1.0,1.1)", CompiledArtifactIdentification.parse(grp + ":" + artB + "#1.0.2"))));
		
		dependencyExpectations.add( Pair.of( "two", Pair.of( grp + ":" + artC + "#[1.0,1.1)", CompiledArtifactIdentification.parse(grp + ":" + artC + "#1.0.3-pc"))));
		dependencyExpectations.add( Pair.of( "two", Pair.of( grp + ":" + artD + "#[1.0,1.1)", CompiledArtifactIdentification.parse(grp + ":" + artD + "#1.0.2"))));
	}
	// pair: first - stage, second CAI
	List<Pair<String,CompiledArtifactIdentification>> artifactExpectations = new ArrayList<>();
	{
		artifactExpectations.add( Pair.of( "one", CompiledArtifactIdentification.parse(grp + ":" + art + "#1.0")));
		
		artifactExpectations.add( Pair.of( "one", CompiledArtifactIdentification.parse(grp + ":" + artA + "#1.0.1")));
		artifactExpectations.add( Pair.of( "one", CompiledArtifactIdentification.parse(grp + ":" + artB + "#1.0.1")));
		artifactExpectations.add( Pair.of( "one", CompiledArtifactIdentification.parse(grp + ":" + artC + "#1.0.1")));
		artifactExpectations.add( Pair.of( "one", CompiledArtifactIdentification.parse(grp + ":" + artD + "#1.0.1")));
		
		artifactExpectations.add( Pair.of( "two", CompiledArtifactIdentification.parse(grp + ":" + art + "#2.0")));
		artifactExpectations.add( Pair.of( "two", CompiledArtifactIdentification.parse(grp + ":" + artA + "#1.0.2")));
		artifactExpectations.add( Pair.of( "two", CompiledArtifactIdentification.parse(grp + ":" + artB + "#1.0.2")));
		artifactExpectations.add( Pair.of( "two", CompiledArtifactIdentification.parse(grp + ":" + artC + "#1.0.2")));
		artifactExpectations.add( Pair.of( "two", CompiledArtifactIdentification.parse(grp + ":" + artD + "#1.0.2")));		
	}
	
	// pair : first - stage, second pair: first - CAI, second - pair: first - PI, second - present/notpresent 
	List<Pair<String, Pair<CompiledArtifactIdentification, Pair<PartIdentification, Boolean>>>> partExpectations = new ArrayList<>();
	{
		partExpectations.add( Pair.of( "one", Pair.of(CompiledArtifactIdentification.parse(grp + ":" + art + "#1.0"), Pair.of(PartIdentification.of("pom"), true))));
		partExpectations.add( Pair.of( "one", Pair.of(CompiledArtifactIdentification.parse(grp + ":" + art + "#1.0"), Pair.of(PartIdentification.of("jar"), true))));
		partExpectations.add( Pair.of( "one", Pair.of(CompiledArtifactIdentification.parse(grp + ":" + art + "#1.0"), Pair.of(PartIdentification.create("sources", "jar"), true))));
		partExpectations.add( Pair.of( "one", Pair.of(CompiledArtifactIdentification.parse(grp + ":" + art + "#1.0"), Pair.of(PartIdentification.create("javadoc", "jar"), false))));
		

		partExpectations.add( Pair.of( "one", Pair.of(CompiledArtifactIdentification.parse(grp + ":" + artA + "#1.0.1"), Pair.of(PartIdentification.create("javadoc", "jar"), false))));		
		partExpectations.add( Pair.of( "two", Pair.of(CompiledArtifactIdentification.parse(grp + ":" + artA + "#1.0.1"), Pair.of(PartIdentification.create("javadoc", "jar"), true))));

		
	}
	
	private List<String> stages = Arrays.asList("one", "two");
	
	protected LazyInitialized<CloseableHttpClient> httpClient = new LazyInitialized<>( RepoletUsingTrait::client);
	
	
	@Before 
	public void before() {		
		TestUtils.ensure(output);
		TestUtils.copy(preparedInitialRepository, localRepository);
		
		runBefore(launcher);
	}
	
	@After
	public void after() {
		runAfter( launcher);
	}
	
	@Override
	public void log(String message) {	
		log.debug(message);
	}
	
	@Test
	public void runTest() {		
		for (String stage : stages) {
			System.out.println("running stage [" + stage + "]");
			try {
				setupStage( stage);
				runResolvingTest( stage);							
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail("exception thrown " + e.getLocalizedMessage() + "]");
			}
		}
	}
		
	private void setupStage(String state) {
		LauncherCfg cfg = launcher.getLaunchedCfg();
		for (RepoletCfg rcfg : cfg.getRepoletCfgs()) {			
			String url = "http://localhost:${port}/" + rcfg.getName() + "/update";
			url = url.replace("${port}", "" + cfg.getPort());			
			try {
				CloseableHttpResponse response = RepoletUsingTrait.retrieveGetResponse( httpClient.get(), url + "?key=" + state);
				HttpEntity entity = response.getEntity();
				EntityUtils.consume(entity);
			} catch (IOException e) {	
				e.printStackTrace();
				Assert.fail("exception thrown while switching :" + e.getMessage());
			}
		}
		
	}

	private void runResolvingTest(String stage) throws Exception {
		
		OverridingEnvironment ves = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		ves.setEnv("repo", localRepository.getAbsolutePath());
		ves.setEnv( "port", Integer.toString( launcher.getAssignedPort()));
		ves.setEnv("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settings.getAbsolutePath());

		try (
				
				WireContext<ArtifactDataResolverContract> resolverContext = Wire.contextBuilder( ArtifactDataResolverModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
					.bindContract(VirtualEnvironmentContract.class, () -> ves)				
					.build();
		) {
			
			ArtifactDataResolverContract artifactDataResolverContract = resolverContext.contract();
			
			runDependencyResolverTest( stage, artifactDataResolverContract);
			
			runArtifactResolverTest( stage, artifactDataResolverContract);
			
			runPartResolvingTest( stage, artifactDataResolverContract);
						
		}
				
	}

	private void runPartResolvingTest(String stage, ArtifactDataResolverContract artifactDataResolverContract) {
		ArtifactPartResolver artifactPartResolver = artifactDataResolverContract.artifactResolver();
		
		for (Pair<String, Pair<CompiledArtifactIdentification, Pair<PartIdentification, Boolean>>> pair : partExpectations) {
			if (!pair.first.equalsIgnoreCase(stage)) {
				continue;
			}			
			CompiledArtifactIdentification cai = pair.second.first;
			Pair<PartIdentification, Boolean> piToPathPair = pair.second.second;
			PartIdentification pi = piToPathPair.first;
			
			String path = null;
			if (piToPathPair.second) {
				path = ArtifactAddressBuilder.build().root(localRepository.getAbsolutePath()).compiledArtifact(cai).part(pi).toPath().toFilePath();
			}
			validateResolving(artifactPartResolver, cai, pi, path);			
		}								
	}
	
	private void validateResolving(ArtifactPartResolver resolver, CompiledArtifactIdentification cai, PartIdentification pi, String path) {
		Maybe<ArtifactDataResolution> pomOptional = resolver.resolvePart(cai, pi);
		CompiledPartIdentification cpi = CompiledPartIdentification.from(cai, pi);
		if (path != null) {		
			Assert.assertTrue("expected to find [" + cpi.asString() + "], yet found nothing", pomOptional.isSatisfied());								
			
			Resource resource = pomOptional.get().getResource();
			if (resource instanceof FileResource) {
				FileResource fresource = (FileResource) resource;
				Assert.assertTrue("expected path is [" + path + "], yet found [" + fresource.getPath() + "]" , path.equalsIgnoreCase( fresource.getPath()));
			}
		}
		else {
			Assert.assertTrue("expected not to find [" + cpi.asString() + "], yet found it", !pomOptional.isSatisfied());
		}
		
		
	}
	private void runArtifactResolverTest(String stage, ArtifactDataResolverContract artifactDataResolverContract) {
		CompiledArtifactResolver compiledArtifactResolver = artifactDataResolverContract.redirectAwareCompiledArtifactResolver();
		
		for (Pair<String, CompiledArtifactIdentification> pair : artifactExpectations) {
			if (!pair.first.equalsIgnoreCase( stage)) {
				continue;
			}
			CompiledArtifactIdentification expected = pair.second;
			Maybe<CompiledArtifact> resolved = compiledArtifactResolver.resolve( expected);
			Assert.assertTrue("expected [" + expected.asString() + "], found nothing", resolved.isSatisfied());
			Assert.assertTrue("expected [" + expected.asString() + "], found [" + resolved.get().asString() +"]", resolved.get().compareTo( expected) == 0);			
		}				
	}

	private void runDependencyResolverTest(String stage, ArtifactDataResolverContract artifactDataResolverContract) {
		DependencyResolver dependencyResolver = artifactDataResolverContract.dependencyResolver();
		
		for ( Pair<String, Pair<String, CompiledArtifactIdentification>> stagedPair: dependencyExpectations) {
			if (!stagedPair.first.equals( stage)) {
				continue;
			}
			Pair<String, CompiledArtifactIdentification> pair = stagedPair.second;
			if (pair == null) {
				Assert.fail("no expectations found for stage [" + stage + "]");
				return;
			}
						
			CompiledDependencyIdentification cdi = CompiledDependencyIdentification.parse( pair.first);		
			Maybe<CompiledArtifactIdentification> resolvedDependency = dependencyResolver.resolveDependency( cdi);
			Assert.assertTrue("expected [" + pair.second.asString() + "], found nothing", resolvedDependency.isSatisfied());
			
			CompiledArtifactIdentification cai = resolvedDependency.get();
			Assert.assertTrue("expected [" + pair.second.asString() + "], found [" + cai.asString() + "]", cai.asString().equals( pair.second.asString()));
		}
		
	}
	
}
