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
package com.braintribe.devrock.mc.core.wired.repository.simple;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.resolver.ArtifactDataResolverModule;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.devrock.repolet.launcher.LauncherTrait;
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
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * tests middle tier features in a multi-thread environment..
 * @author pit
 *
 */
@Category(KnownIssue.class)
public class ThreadedCompoundResolvingTest implements LauncherTrait, HasCommonFilesystemNode {
	private static Logger log = Logger.getLogger(ThreadedCompoundResolvingTest.class);
	private static final int MAX_THREADS = 5;
	
	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots( getRoot());
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	
	
	private File resolverRepositoryA = new File( input, "remoteRepoA");
	private File resolverRepositoryB = new File( input, "remoteRepoB");
	private File preparedInitialRepository = new File( input, "initial");
	
	private File settings = new File( input, "settings/basic-settings.xml");

	private static String grp = "com.braintribe.devrock.test";
	private static String art = "artifact";
	
	private Launcher launcher = Launcher.build()
					.repolet()
						.name("archiveA")
						//.changesUrl("http://localhost:${port}/archiveA/rest/changes")
						.serverIdentification("repolet1")
						.filesystem()
							.filesystem(resolverRepositoryA)
						.close()
					.close()
					.repolet()
						.name("archiveB")
						//.changesUrl("http://localhost:${port}/archiveB/rest/changes")
						.serverIdentification("repolet2")
						.filesystem()
							.filesystem( resolverRepositoryB)
						.close()
					.close()
				.done();	
	
	
	private List<CompiledArtifactIdentification> cais;
	{
		cais = new ArrayList<>();
		cais.add( CompiledArtifactIdentification.parse(grp + ":" + art + "#1.0"));
		cais.add( CompiledArtifactIdentification.parse(grp + ":" + art + "#2.0"));
		cais.add( CompiledArtifactIdentification.parse(grp + ":" + art + "#3.0"));
	}
	
	protected String getRoot() {
		return "wired/threadedResolving";
	}
	
	@Before 
	public void before() {		
		TestUtils.ensure(output);
		TestUtils.copy(preparedInitialRepository, repo);
		
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
	public void threadedResolvingTest() throws Exception {
		
		OverridingEnvironment ves = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		ves.setEnv("repo", repo.getAbsolutePath());
		ves.setEnv( "port", Integer.toString( launcher.getAssignedPort()));
		ves.setEnv("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settings.getAbsolutePath());

		
		try (
				
				WireContext<ArtifactDataResolverContract> resolverContext = Wire.contextBuilder( ArtifactDataResolverModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
					.bindContract(VirtualEnvironmentContract.class, () -> ves)				
					.build();
		) {
			
			ArtifactDataResolverContract artifactDataResolverContract = resolverContext.contract();
			
			runDependencyResolverTest( artifactDataResolverContract);
			
			runArtifactResolverTest( artifactDataResolverContract);
			
			runPartResolvingTest( artifactDataResolverContract);
						
		}				
	}

	/**
	 * threaded part resolving test
	 * @param artifactDataResolverContract
	 */
	private void runPartResolvingTest(ArtifactDataResolverContract artifactDataResolverContract) {
		ArtifactPartResolver artifactPartResolver = artifactDataResolverContract.artifactResolver();
		
		CompiledPartIdentification pomPi = CompiledPartIdentification.from(cais.get(0), PartIdentification.of("pom"));
		CompiledPartIdentification jarPi = CompiledPartIdentification.from(cais.get(1), PartIdentification.of("jar"));
		CompiledPartIdentification javadocJarPi = CompiledPartIdentification.from(cais.get(1), PartIdentification.create("javadoc", "jar"));
		CompiledPartIdentification sourcesJarPi = CompiledPartIdentification.from(cais.get(2), PartIdentification.create("sources", "jar"));
		
		Map<CompiledPartIdentification,String> piToPathMap = new HashMap<>();
		piToPathMap.put(pomPi, ArtifactAddressBuilder.build().root(repo.getAbsolutePath()).compiledArtifact(cais.get(0)).part(pomPi).toPath().toFilePath());
		piToPathMap.put(jarPi, ArtifactAddressBuilder.build().root(repo.getAbsolutePath()).compiledArtifact(cais.get(1)).part( jarPi).toPath().toFilePath());
		piToPathMap.put( javadocJarPi, "<none>");
		piToPathMap.put( sourcesJarPi, ArtifactAddressBuilder.build().root(repo.getAbsolutePath()).compiledArtifact(cais.get(2)).part(sourcesJarPi).toPath().toFilePath());
		
		
		Map<CompiledPartIdentification, Future<Maybe<ArtifactDataResolution>>> futures = new HashMap<>();
		ExecutorService es =  Executors.newFixedThreadPool( Math.max( cais.size(), MAX_THREADS));
		for (CompiledPartIdentification pi : piToPathMap.keySet()) {
			futures.put( pi, es.submit( () -> artifactPartResolver.resolvePart(pi, pi)));
		}
		
		es.shutdown();
		try {
			es.awaitTermination(1000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for (Map.Entry<CompiledPartIdentification, Future<Maybe<ArtifactDataResolution>>> entry : futures.entrySet()) {
			CompiledPartIdentification cpi = entry.getKey();
			Maybe<ArtifactDataResolution> optional;
			try {
				optional = entry.getValue().get();
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail("exception thrown");
				return;				
			}
			String path = piToPathMap.get(cpi);
			if (!path.equals("<none>")) {		
				Assert.assertTrue("expected to find [" + cpi.asString() + "], yet found nothing",optional.isSatisfied());								
				
				Resource resource = optional.get().getResource();
				if (resource instanceof FileResource) {
					FileResource fresource = (FileResource) resource;
					Assert.assertTrue("expected path is [" + path + "], yet found [" + fresource.getPath() + "]" , path.equalsIgnoreCase( fresource.getPath()));
				}
			}
			else {
				Assert.assertTrue("expected not to find [" + cpi.asString() + "], yet found it", !optional.isSatisfied());
			}		
		}							
	}
		
	/**
	 * threaded artifact resolver test 
	 * @param artifactDataResolverContract
	 */
	private void runArtifactResolverTest(ArtifactDataResolverContract artifactDataResolverContract) {
		CompiledArtifactResolver compiledArtifactResolver = artifactDataResolverContract.redirectAwareCompiledArtifactResolver();
		
		ExecutorService es =  Executors.newFixedThreadPool( Math.max( cais.size(), MAX_THREADS));
		
		Map<CompiledArtifactIdentification, Future<CompiledArtifact>> futures = new HashMap<>();
		for (CompiledArtifactIdentification cai : cais) {
			futures.put( cai, es.submit( () -> compiledArtifactResolver.resolve(cai).get()));
		}
		
		es.shutdown();
		try {
			es.awaitTermination(1000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for (Map.Entry<CompiledArtifactIdentification, Future<CompiledArtifact>> entry : futures.entrySet()) {
			try {
				CompiledArtifact compiledArtifact = entry.getValue().get();
				CompiledArtifactIdentification key = entry.getKey();
				Assert.assertTrue("expected [" + key.asString() + "], found [" + compiledArtifact.asString() +"]", compiledArtifact.compareTo( key) == 0);
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail("exception thrown");
			}
		}
						
	}

	/**
	 * threaded dependency resolver test 
	 * @param artifactDataResolverContract
	 */
	private void runDependencyResolverTest(ArtifactDataResolverContract artifactDataResolverContract) {
		DependencyResolver dependencyResolver = artifactDataResolverContract.dependencyResolver();
		
		List<CompiledDependencyIdentification> cdis = Arrays.asList( 
				CompiledDependencyIdentification.parse( grp + ":" + art + "#[1.0,2.0)"),
				CompiledDependencyIdentification.parse( grp + ":" + art + "#[2.0,3.0)"),
				CompiledDependencyIdentification.parse( grp + ":" + art + "#[3.0,4.0)")
				);
		
		ExecutorService es =  Executors.newFixedThreadPool( Math.max( cdis.size(), MAX_THREADS));
		
		Map<CompiledDependencyIdentification, Future<Maybe<CompiledArtifactIdentification>>> futures = new HashMap<>();
		for (CompiledDependencyIdentification cdi : cdis) {
			futures.put( cdi, es.submit( () ->  dependencyResolver.resolveDependency( cdi)));
		}
		
		es.shutdown();
		try {
			es.awaitTermination(1000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for (Map.Entry<CompiledDependencyIdentification, Future<Maybe<CompiledArtifactIdentification>>> future : futures.entrySet()) {
			try {
				Maybe<CompiledArtifactIdentification> optional = future.getValue().get();
				Assert.assertTrue("expected [" + future.getKey()  +"] to be available",  optional.isSatisfied());
			} catch (Exception e) {			
				e.printStackTrace();
				Assert.fail("exception thrown");
			}
		}		
	}	
}
