// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
package com.braintribe.devrock.mc.core.wired.resolving.transitive.partreflection;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.resolver.PartAvailabilityReflection;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.configuration.RepositoryConfigurationLoader;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.consumable.PartReflection;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * abstract base class for the part-availability extraction test
 * @author pit
 *
 */
public abstract class AbstractPartReflectionTest implements HasCommonFilesystemNode {
	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/partreflection");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	protected File settings = new File(input, "settings.xml");
	protected File initial = new File( input, "local-repo");
	
	protected Launcher launcher; 
	
	protected TransitiveResolutionContext lenientContext = TransitiveResolutionContext.build().lenient(true).done();
	
	protected void additionalTasks() {}
	
	@Before
	public void runBefore() {
		
		TestUtils.ensure(repo); 			
		launcher.launch();		
		additionalTasks();
	}
	
	@After
	public void runAfter() {
		launcher.shutdown();
	}
	

	protected RepoletContent archiveInput(String definition) {
		File file = new File( input, definition);
		try {
			return RepositoryGenerations.parseConfigurationFile(file);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot load parser file [" + file.getAbsolutePath() + "]" , IllegalStateException::new);
		} 
	}
	
	protected OverridingEnvironment buildVirtualEnvironement(Map<String,String> overrides) {
		OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		if (overrides != null && !overrides.isEmpty()) {
			ove.setEnvs(overrides);						
		}
		ove.setEnv("repo", repo.getAbsolutePath());
		ove.setEnv(RepositoryConfigurationLoader.ENV_DEVROCK_REPOSITORY_CONFIGURATION, null);
		ove.setEnv("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settings.getAbsolutePath());
		ove.setEnv( "port", Integer.toString( launcher.getAssignedPort()));
				
		return ove;		
	}

	/**
	 * run a resolution first and then extract the locally known part availability 
	 * @param terminalAsString - the terminal for the resolution 
	 * @param availabilityTargetAsString - the availability target
	 * @return - a {@link Map} of the repoid to a {@link Set} of the found {@link CompiledPartIdentification}
	 */
	protected List<PartReflection> runResolveViaSettings(String availabilityTargetAsString) {
		try (				
				WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
					.bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement(null))				
					.build();
			) {
			
			
			// retrieve the part availability data
			CompiledArtifactIdentification compiledTargetIdentification = CompiledArtifactIdentification.parse(availabilityTargetAsString);
			
			
			PartAvailabilityReflection partAvailabilityReflection = resolverContext.contract().dataResolverContract().partAvailabilityReflection();
			List<PartReflection> partsOf = partAvailabilityReflection.getAvailablePartsOf(compiledTargetIdentification);
						
			return partsOf;
											
		}
		catch( Exception e) {
			e.printStackTrace();
			Assert.fail("exception thrown [" + e.getLocalizedMessage() + "]");		
		}
		return null;
	}
	
	
	/**
	 * validates two part-availability extractions (found real, expected constructed)
	 * @param found - a {@link Map} of the repoid to a {@link Set} of the found {@link CompiledPartIdentification}
	 * @param expected - a {@link Map} of the repoid to a {@link Set} of the found {@link CompiledPartIdentification}
	 * @return - true if matches, otherwise false (will actually throw an assertion failure)
	 */
	protected boolean validate( List<PartReflection> found, List<PartReflection> expected) {
		List<String> assertions = new ArrayList<>();
		
		if (found.size() != expected.size()) {
			assertions.add("size of found data expected to be [" + expected.size() + "] yet it is [" + found.size() + "]");
		}
		
	
		List<String> foundParts = found.stream().map( cp -> cp.asString()).collect(Collectors.toList());			
		List<String> expectedParts = expected.stream().map( cp -> cp.asString()).collect( Collectors.toList());
		
		List<String> matching = new ArrayList<>( foundParts.size());
		List<String> excess = new ArrayList<>( foundParts.size());			
		for (String foundCpi : foundParts) {
			if (expectedParts.contains( foundCpi)) {
				matching.add( foundCpi);
			}
			else {
				excess.add( foundCpi);
			}
		}
		List<String> missing = new ArrayList<>( expectedParts);
		missing.removeAll( matching);
		
		if (missing.size() > 0) {
			assertions.add( "[" + missing.size() + "] missing : " + missing.stream().collect(Collectors.joining(",")));
		}
		if (excess.size() > 0) {
			assertions.add( "[" + excess.size() + "] excess : " + excess.stream().collect(Collectors.joining(",")));
		}
	
		
		if (assertions.size() > 0) {
			Assert.fail( assertions.stream().collect( Collectors.joining("\n")));
			return false;		
		}
		return true;
	}
	
	
}
