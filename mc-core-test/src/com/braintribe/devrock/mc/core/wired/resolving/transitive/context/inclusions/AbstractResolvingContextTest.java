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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.context.inclusions;

import java.io.File;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * abstract base class for all inclusion tests: specifying TDR's inclusions (parents/imports/relocations)
 * 
 * @author pit
 *
 */
public abstract class AbstractResolvingContextTest implements HasCommonFilesystemNode {
	protected static final String terminal = "com.braintribe.devrock.test:t#1.0.1";
	protected static final String COMMON_CONTEXT_DEFINITION_YAML = "archive.definition.yaml";

	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/context/inclusions");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	protected File settings = new File(input, "settings.xml");
	protected File initial = new File( input, "local-repo");
	
	private Launcher launcher; 
	{
		launcher = Launcher.build()
				.repolet()
				.name("archive")
					.descriptiveContent()
						.descriptiveContent(archiveInput())
					.close()
				.close()
			.done();
	}
	
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
	
	
	/**
	 * standard content loader, override if other content's required
	 * @return
	 */
	protected RepoletContent archiveInput() {
		return archiveInput(COMMON_CONTEXT_DEFINITION_YAML);
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
		ove.setEnv("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settings.getAbsolutePath());
		ove.setEnv( "port", Integer.toString( launcher.getAssignedPort()));
				
		return ove;		
	}

	
	/**
	 * run a standard transitive resolving 
	 * @param terminal - the String of the terminal
	 * @param resolutionContext - the {@link ClasspathResolutionContext}
	 * @return - the resulting {@link AnalysisArtifactResolution}
	 */
	protected AnalysisArtifactResolution run(String terminal, TransitiveResolutionContext resolutionContext) throws Exception {
		try (				
				WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
					.bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement(null))				
					.build();
			) {
			
			TransitiveDependencyResolver transitiveResolver = resolverContext.contract().transitiveDependencyResolver();
			
			CompiledTerminal cdi = CompiledTerminal.from ( CompiledDependencyIdentification.parse( terminal));
			AnalysisArtifactResolution artifactResolution = transitiveResolver.resolve( resolutionContext, cdi);
			return artifactResolution;													
		}		
	}
	
	/**
	 * @param terminal - the terminal as a String
	 * @param trc - the {@link TransitiveResolutionContext}
	 * @param validationFile - the name of the validation file
	 * @return - the {@link AnalysisArtifactResolution}
	 */
	protected AnalysisArtifactResolution runAndValidate( String terminal, TransitiveResolutionContext trc, String validationFile) {
		return runAndValidate(terminal, trc, validationFile, false);
	}
	
	/**
	 * runs and validates a run on the TDR
	 * @param terminal - the terminal as a String
	 * @param trc - the {@link TransitiveResolutionContext}
	 * @param validationFile - the name of the validation file 
	 * @param validateParts - true if parts should be validated
	 */
	protected AnalysisArtifactResolution runAndValidate( String terminal, TransitiveResolutionContext trc, String validationFile, boolean validateParts) {
		try {
			AnalysisArtifactResolution resolution = run( terminal, trc);
			
			Validator validator = new Validator();			
			validator.validateYaml( new File( input, validationFile), resolution, validateParts, false);			
			validator.assertResults();
			return resolution;
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("exception thrown " + e.getLocalizedMessage());
		}			
		return null;
	}
	
}
