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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.rules.tag;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.classpath.ClasspathDependencyResolver;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContextBuilder;
import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContextBuilder;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.resolver.rulefilter.BasicTagRuleFilter;
import com.braintribe.devrock.mc.core.resolver.rulefilter.TagRuleFilter;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.mc.core.wirings.classpath.ClasspathResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.classpath.contract.ClasspathResolverContract;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.resolver.contract.ArtifactDataResolverContract;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * abstract base class for tag rule tests with TDR & repolet
 * @author pit
 *
 */
public abstract class AbstractTagLab implements HasCommonFilesystemNode  {
	protected File repo;
	protected File input;
	protected File output;
	
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/rules");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	
	private File settings = new File( input, "settings.xml");
	
	protected TransitiveResolutionContext standardTransitiveResolutionContext = TransitiveResolutionContext.build().lenient( true).done();
	protected ClasspathResolutionContext standardClasspathResolutionContext = ClasspathResolutionContext.build().lenient(false).done();
			
	
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
	
	
	
	/**
	 * to be overloaded if more should happen before the test is run
	 */
	protected void additionalSetupTask() {}

	private RepoletContent archiveInput() {
		try {
			return RepositoryGenerations.unmarshallConfigurationFile( new File( input, "archive.definition.yaml"));
		} catch (Exception e) {			
			Assert.fail("cannot load defintion file as [" + e.getMessage() + "]");
			return null;
		} 
	}

	@Before
	public void runBefore() {
		TestUtils.ensure(repo); 	
		additionalSetupTask();
		launcher.launch();
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
		ove.setEnv("M2_REPO", repo.getAbsolutePath());
		ove.setEnv("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settings.getAbsolutePath());
		ove.setEnv( "port", Integer.toString( launcher.getAssignedPort()));
				
		return ove;		
	}
	
	/**
	 * run a classpath resolving 
	 * @param terminal - the String of the terminal
	 * @param resolutionContext - the {@link ClasspathResolutionContext}
	 * @return - the resulting {@link AnalysisArtifactResolution}
	 */
	protected AnalysisArtifactResolution run(String terminal, ClasspathResolutionContext resolutionContext, boolean asArtifact) throws Exception {
		try (				
				WireContext<ClasspathResolverContract> resolverContext = Wire.contextBuilder( ClasspathResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
					.bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement(null))				
					.build();
			) {
			
			CompiledTerminal cti;
			CompiledDependencyIdentification cdi = CompiledDependencyIdentification.parse( terminal);
			if (asArtifact) {			
				ArtifactDataResolverContract contract = resolverContext.contract().transitiveResolverContract().dataResolverContract();
				Maybe<CompiledArtifactIdentification> caiMaybe = contract.dependencyResolver().resolveDependency(cdi);
				if (caiMaybe.isUnsatisfied()) {
					Assert.fail("cannot resolve terminal dependency " + terminal);
					return null;
				}
				Maybe<CompiledArtifact> caMaybe = contract.directCompiledArtifactResolver().resolve(caiMaybe.get());
				
				if (caMaybe.isUnsatisfied()) {
					Assert.fail("cannot resolve terminal artifact " + caiMaybe.get().asString());
					return null;	
				}
				cti = CompiledTerminal.from ( caMaybe.get());
			}
			else {
				cti = CompiledTerminal.from(cdi);
			}
			
			ClasspathDependencyResolver classpathResolver = resolverContext.contract().classpathResolver();
			
	
			AnalysisArtifactResolution artifactResolution = classpathResolver.resolve( resolutionContext, cti);
			return artifactResolution;					
								
		}			
	}
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
			
			
			CompiledTerminal cti;
			CompiledDependencyIdentification cdi = CompiledDependencyIdentification.parse( terminal);
			if (asArtifact) {			
				ArtifactDataResolverContract contract = resolverContext.contract().dataResolverContract();
				Maybe<CompiledArtifactIdentification> caiMaybe = contract.dependencyResolver().resolveDependency(cdi);
				if (caiMaybe.isUnsatisfied()) {
					Assert.fail("cannot resolve terminal dependency " + terminal);
					return null;
				}
				Maybe<CompiledArtifact> caMaybe = contract.directCompiledArtifactResolver().resolve(caiMaybe.get());
				
				if (caMaybe.isUnsatisfied()) {
					Assert.fail("cannot resolve terminal artifact " + caiMaybe.get().asString());
					return null;	
				}
				cti = CompiledTerminal.from ( caMaybe.get());
			}
			else {
				cti = CompiledTerminal.from(cdi);
			}
			
			
			AnalysisArtifactResolution artifactResolution = transitiveResolver.resolve( resolutionContext, cti);
			return artifactResolution;					
								
		}		
	}
			
	protected AnalysisArtifactResolution runTestOnTdr(String terminalAsString, String [] expectedNames, String tagRule, boolean asArtifact) {

		TransitiveResolutionContextBuilder builder = TransitiveResolutionContext.build();
		
		//
		if (tagRule != null) {
			Maybe<BasicTagRuleFilter> ruleFilterPotential = BasicTagRuleFilter.parse(tagRule);
			
			if (ruleFilterPotential.isSatisfied()) {
				TagRuleFilter tagRuleFilter = ruleFilterPotential.get();
				builder.dependencyPathFilter( tagRuleFilter);
			}
			else {
				Assert.fail("cannot build a valid tag rule filter from [" + tagRule + "]");
				return null;
			}
		}		
			
		TransitiveResolutionContext transitiveResolutionContext = builder.done();
			 
		AnalysisArtifactResolution resolution;
		try {
			resolution = run( terminalAsString, transitiveResolutionContext, asArtifact);
		} catch (Exception e) {
			Assert.fail("cannot run resolution as [" + e.getMessage() + "]"); 
			return null;
		}
		validate( resolution.getSolutions(), expectedNames);		
		return resolution;
	}

	protected AnalysisArtifactResolution runTestOnCpr(String terminalAsString, String [] expectedNames, String tagRule, boolean asArtifact) {

		ClasspathResolutionContextBuilder builder = ClasspathResolutionContext.build();
		
		if (tagRule != null) {
			Maybe<BasicTagRuleFilter> ruleFilterPotential = BasicTagRuleFilter.parse(tagRule);
			
			if (ruleFilterPotential.isSatisfied()) {
				TagRuleFilter tagRuleFilter = ruleFilterPotential.get();
				builder.dependencyPathFilter( tagRuleFilter);
			}
			else {
				Assert.fail("cannot build a valid tag rule filter from [" + tagRule + "]");
				return null;
			}
		}		
		
		ClasspathResolutionContext classpathResolutionContext = builder.done();
			 
		AnalysisArtifactResolution resolution;
		try {
			resolution = run( terminalAsString, classpathResolutionContext, asArtifact);
		} catch (Exception e) {
			Assert.fail("cannot run resolution as [" + e.getMessage() + "]"); 
			return null;
		}
		validate( resolution.getSolutions(), expectedNames);
		return resolution;		
	}


	protected boolean validate( List<AnalysisArtifact> solutions, String [] expectedNames) {
		Validator validator = new Validator();
		List<String> found = solutions.stream().map( aa -> aa.asString()).collect( Collectors.toList());
		List<String> expected = Arrays.asList( expectedNames);
		
		List<String> matches = new ArrayList<>(expected.size());
		List<String> excess = new ArrayList<>( found.size());
		for (String suspect : found) {
			if (expected.contains(suspect)) {
				matches.add(suspect);
			}
			else {
				excess.add( suspect);
			}
		}
		List<String> missing = new ArrayList<>( expected);
		missing.removeAll( matches);				
		validator.assertTrue("missing [" + missing.stream().collect( Collectors.joining(",")) + "]", missing.size() == 0);
		
		validator.assertTrue("excess [" + excess.stream().collect( Collectors.joining(",")) + "]", excess.size() == 0);
	
		return validator.assertResults();
		
	}
}
