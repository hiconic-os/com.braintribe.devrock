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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.origination;

import java.io.File;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.devrock.repolet.launcher.LauncherTrait;
import com.braintribe.devrock.repolet.launcher.builder.api.LauncherCfgBuilderContext;
import com.braintribe.exception.Exceptions;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * base class for 'real time configuration' tests - it's not aimed at actually running resolutions, but actually 
 * to access the repository configuration. It is using the {@link MavenConfigurationWireModule} to get the basics 
 * (could be a dev-env or direct YAML one) - the rest comes from the setup (filters, bias et al). 
 * 
 * - currently only test based on it is a test to check if the origination is correct when a .pc_bias file is 
 * involved. 
 *  
 * @author pit
 *
 */
public abstract class AbstractTransitiveRepositoryConfigurationTest implements LauncherTrait, HasCommonFilesystemNode {	
	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/origination");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	
	protected File initial = new File( input, "initial");
	
	protected TransitiveResolutionContext standardResolutionContext = TransitiveResolutionContext.build().done();
	protected String terminal = "com.braintribe.devrock.test:t#1.0.1";
			
	/**
	 * @return - a {@link Map} of 'repolet name' to {@link RepoletContent}
	 */
	protected abstract Map<String, RepoletContent> archives();
	/**
	 * @return - the {@link File} pointing to the pc-bias file (any name does do) or null if none
	 */
	protected abstract File biasFileInput(); 
	/**
	 * @return - the {@link File} pointing to the maven settings.xml
	 */
	protected abstract File settings();
	
	// gets multiple repolet definitions from deriving class
	private Launcher launcher; 
	{		
		Map<String, RepoletContent> map = archives();		
		LauncherCfgBuilderContext bcfg = Launcher.build();		
		for (Map.Entry<String, RepoletContent> entry : map.entrySet()) {
			bcfg.repolet()
					.name( entry.getKey())
					.descriptiveContent()
						.descriptiveContent( entry.getValue())
					.close()
				.close();					
		}
		launcher = bcfg.done();
				
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
	
	protected RepoletContent archiveInput(String definition) {
		File file = new File( input, definition);
		try {
			return RepositoryGenerations.unmarshallConfigurationFile(file);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot load parser file [" + file.getAbsolutePath() + "]" , IllegalStateException::new);
		} 
	}
	
	protected OverridingEnvironment buildVirtualEnvironement(Map<String,String> overrides) {
		OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		if (overrides != null && !overrides.isEmpty()) {
			ove.setEnvs(overrides);						
		}
		ove.setEnv("M2_REPO", repo.getAbsolutePath());
		ove.setEnv("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settings().getAbsolutePath());
		ove.setEnv( "port", Integer.toString( launcher.getAssignedPort()));
				
		return ove;		
	}
	
	protected RepositoryConfiguration compileConfiguration() {
		try (				
				WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
					.bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement(null))				
					.build();
			) {			
			return resolverContext.contract().dataResolverContract().repositoryReflection().getRepositoryConfiguration();													
		}
		catch( Exception e) {
			e.printStackTrace();
			Assert.fail("exception thrown [" + e.getLocalizedMessage() + "]");		
		}
		return null;
	}
	


}

