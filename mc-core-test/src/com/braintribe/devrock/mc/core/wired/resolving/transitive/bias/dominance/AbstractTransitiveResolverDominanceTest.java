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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.bias.dominance;

import java.io.File;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.configuration.StandaloneRepositoryConfigurationLoader;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.devrock.repolet.launcher.LauncherTrait;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

public abstract class AbstractTransitiveResolverDominanceTest implements LauncherTrait, HasCommonFilesystemNode {	
	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/bias/dominance");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	
	protected File initial = new File( input, "initial");
	
	protected TransitiveResolutionContext standardResolutionContext = TransitiveResolutionContext.build().done();
	
	protected String terminalOne = "com.braintribe.devrock.test:terminal#1.0.1";
	protected String terminalTwo = "com.braintribe.devrock.test:terminal#1.0.2";
	protected String terminalThree = "com.braintribe.devrock.test:terminal#1.0.3";
			
	protected File biasFileInput() { return null;} 	
	
	private Launcher launcher; 
	{
		launcher = Launcher.build()
			.repolet()
				.name("archive")
				.descriptiveContent()
					.descriptiveContent( archiveInput( new File( input, "archive.content.yaml")))
				.close()
			.close()
			.repolet()
				.name("dominant")
					.descriptiveContent()
						.descriptiveContent( archiveInput( new File( input, "dominant.content.yaml")))
					.close()
			.close()
			.repolet()
				.name("recessive")
					.descriptiveContent()
						.descriptiveContent( archiveInput( new File( input, "recessive.content.yaml")))
					.close()
			.close()
			.done();
	}

	protected RepoletContent archiveInput( File file) {
		//File file = new File( input, "default.repolet.content.yaml");
		try {
			return RepositoryGenerations.unmarshallConfigurationFile(file);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot load parser file [" + file.getAbsolutePath() + "]" , IllegalStateException::new);
		} 
	}
	
	@Before
	public void runBefore() {
		TestUtils.ensure(repo); 			
		launcher.launch();
		// copy initial data (mimic local repository)
		if (initial.exists()) {
			TestUtils.copy( initial, repo);
		}
		File file = biasFileInput();
		if (file != null && file.exists()) {
			TestUtils.copy(file, new File(repo, ".pc_bias"));
		}
	}
	
	@After
	public void runAfter() {
		launcher.shutdown();
	}
	
	protected OverridingEnvironment buildVirtualEnvironement(Map<String,String> overrides) {
		OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		if (overrides != null && !overrides.isEmpty()) {
			ove.setEnvs(overrides);						
		}
		ove.setEnv("cache", repo.getAbsolutePath());	
		ove.setEnv( "port", Integer.toString( launcher.getAssignedPort()));
				
		return ove;		
	}

	protected AnalysisArtifactResolution run(String terminal, TransitiveResolutionContext resolutionContext) {
		VirtualEnvironment ve = buildVirtualEnvironement(null);
		try (				
				WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE)
					.bindContract( RepositoryConfigurationContract.class, () -> loadRepositoryConfiguration(ve))
					.bindContract(VirtualEnvironmentContract.class, () -> ve)				
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
	private Maybe<RepositoryConfiguration> loadRepositoryConfiguration( VirtualEnvironment ve) {
		StandaloneRepositoryConfigurationLoader srcl = new StandaloneRepositoryConfigurationLoader();
		srcl.setVirtualEnvironment(ve);
		return srcl.loadRepositoryConfiguration( new File( input, "repository-configuration.yaml"));
	}
	
	
	

}

