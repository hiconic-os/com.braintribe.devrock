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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.repository.probingstates;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.configuration.RepositoryConfigurationLoader;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.mc.core.wirings.env.configuration.EnvironmentSensitiveConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.model.mc.reason.configuration.ProbingFailed;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * a) the repository-configuration reflects the failures of the repositories contained, 
 * and
 * b) the correct failure reason type is used
 * 
 * abstract test - the derivations are the actual concrete test cases
 * @author pit
 *
 */

public abstract class AbstractRepositoryConfigurationProbingTest implements HasCommonFilesystemNode {

	protected static final String COMMON_CONTEXT_DEFINITION_YAML = "archive.definition.yaml";

	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/repository/probing");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	
	protected File config() { return new File( input, "repository-configuration.yaml");} 
	protected File initial = new File( input, "local-repo");
	
	protected RepoletContent archiveInput() { return RepoletContent.T.create();}
	
	protected abstract Launcher launcher();
	
	private Launcher launcher = launcher(); 
		
	protected void additionalTasks() {}
	
	@Before
	public void runBefore() {
		
		
		TestUtils.ensure(repo); 			

		if (initial.exists()) {
			TestUtils.copy(initial, repo);
		}	
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
		ove.setEnv("repo", repo.getAbsolutePath());
		ove.setEnv(RepositoryConfigurationLoader.ENV_DEVROCK_REPOSITORY_CONFIGURATION, config().getAbsolutePath());
		ove.setEnv( "port", Integer.toString( launcher.getAssignedPort()));
				
		return ove;		
	}

	
	
	protected RepositoryReflection getReflection() throws Exception {
		VirtualEnvironment ove = buildVirtualEnvironement(null);
		try (				
				WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE, new EnvironmentSensitiveConfigurationWireModule( ove)).build();
			) {			
			RepositoryReflection repositoryReflection = resolverContext.contract().dataResolverContract().repositoryReflection();			
			return repositoryReflection;																					
		}
	}
	
protected void validate( RepositoryConfiguration repositoryConfiguration, String failingRepoName, EntityType<?> repoFailureReason) {
		
		Validator validator = new Validator();
		validator.assertTrue( "repository configuration isn't flagged as failed", repositoryConfiguration.hasFailed());
		
		// config
		Reason failure = repositoryConfiguration.getFailure();
		if (failure != null) {
					
			String foundReasonType = failure.entityType().getShortName();	
			String expectedReasonType = ProbingFailed.T.getShortName();
			validator.assertTrue("expected type of reason to be [" +  expectedReasonType + "] yet it is [" + foundReasonType + "]", foundReasonType.equals(expectedReasonType));
			
			List<Reason> reasons = failure.getReasons();
			validator.assertTrue("expected only one failed archive, but found [" + reasons.size(), reasons.size() == 1);
			
			Reason failingRepositoryReason = reasons.get(0);
			String foundRepoReasonType = failingRepositoryReason.entityType().getShortName();	
			String expectedRepoReasonType = repoFailureReason.getShortName();
			validator.assertTrue("expected type of reason to be [" +  expectedRepoReasonType + "] yet it is [" + foundRepoReasonType + "]", foundRepoReasonType.equals(expectedRepoReasonType));
		}
		
		// repository
		for (Repository repository : repositoryConfiguration.getRepositories()) {
			String name = repository.getName();
			if (name.equals(failingRepoName)) {
				validator.assertTrue("unexpectedly, repository [" + name + "] is not flagged as failed", repository.hasFailed());
				Reason repositoryFailureReason = repository.getFailure();
				if (repositoryFailureReason != null) {
					String foundRepoReasonType = repositoryFailureReason.entityType().getShortName();	
					String expectedRepoReasonType = repoFailureReason.getShortName();
					validator.assertTrue("expected type of reason to be [" +  expectedRepoReasonType + "] yet it is [" + foundRepoReasonType + "]", foundRepoReasonType.equals(expectedRepoReasonType));
				}										
			}
			else {
				validator.assertTrue("unexpectedly, repository [" + name + "] is flagged as failed", !repository.hasFailed());
			}
		}
		
		validator.assertResults();		
	}

			
	
}
