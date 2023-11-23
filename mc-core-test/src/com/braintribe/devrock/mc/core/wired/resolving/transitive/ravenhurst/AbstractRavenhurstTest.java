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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.ravenhurst;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.RepoletUsingTrait;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.devrock.repolet.launcher.builder.cfg.LauncherCfg;
import com.braintribe.devrock.repolet.launcher.builder.cfg.RepoletCfg;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.LazyInitialized;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * 
 * abstract test to clarify how mc-core (as wired in real life) processed RH notifications, 
 * especially 'unexpected' notification content. 
 * @author pit
 *
 */
public abstract class AbstractRavenhurstTest implements HasCommonFilesystemNode {
	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/ravenhurst");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	
	private File settings = new File( input, "settings.xml");
	

	protected RepoletContent archiveInput(File file) {	
		try {
			return RepositoryGenerations.unmarshallConfigurationFile( file);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		} 
		return null;
	}
		
	protected Launcher launcher;
	
	protected abstract Launcher launcher();
		
	protected LazyInitialized<CloseableHttpClient> httpClient = new LazyInitialized<>( RepoletUsingTrait::client);
	
	/**
	 * to be overloaded if more should happen before the test is run
	 */
	protected void additionalSetupTask() {}
	
	@Before
	public void runBefore() {
		launcher = launcher();
		
		TestUtils.ensure(repo); 	
		additionalSetupTask();
		launcher.launch();
	}
	
	@After
	public void runAfter() {
		launcher.shutdown();
	}
	
	protected void setupStage(String state) {
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
	
	protected void copyAndPatch(File file, String targetName) {		
		File toPatch = new File( repo, targetName);
		try {
			String contents = IOTools.slurp(file, "UTF-8");
			LauncherCfg cfg = launcher.getLaunchedCfg();			
			String newContents = contents.replace("${env.port}", "" + cfg.getPort());
			IOTools.spit(toPatch, newContents, "UTF-8", false);			
		} catch (IOException e) {		
			e.printStackTrace();
		}						
		
	}

	
	protected OverridingEnvironment buildVirtualEnvironement(Map<String,String> overrides) {
		OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		if (overrides != null && !overrides.isEmpty()) {
			ove.setEnvs(overrides);						
		}
		ove.setEnv("M2_REPO", repo.getAbsolutePath());
		ove.setEnv("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settings.getAbsolutePath());
		ove.setEnv( "port", Integer.toString( launcher.getAssignedPort()));
				
		return ove;		
	}

	protected TransitiveResolutionContext standardTransitiveResolutionContext = TransitiveResolutionContext.build().lenient( true).done();
	/**
	 * run a standard transitive resolving 
	 * @param terminal - the String of the terminal
	 * @param resolutionContext - the {@link ClasspathResolutionContext}
	 * @return - the resulting {@link AnalysisArtifactResolution}
	 */
	
	protected AnalysisArtifactResolution run(String terminal, TransitiveResolutionContext resolutionContext, boolean asArtifact) throws Exception {
		try (				
				WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
					.bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement(null))				
					.build();
			) {
			
			TransitiveDependencyResolver transitiveResolver = resolverContext.contract().transitiveDependencyResolver();
			
			CompiledTerminal cdi;
			if (asArtifact) {
				CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse(terminal);
				Maybe<CompiledArtifact> compiledArtifactOptional = resolverContext.contract().dataResolverContract().directCompiledArtifactResolver().resolve( cai);						
				cdi = compiledArtifactOptional.get();				
			}
			else {
				cdi = CompiledTerminal.from ( CompiledDependencyIdentification.parse( terminal));			
			}
			AnalysisArtifactResolution artifactResolution = transitiveResolver.resolve( resolutionContext, cdi);
			return artifactResolution;													
		}		
	}
}
