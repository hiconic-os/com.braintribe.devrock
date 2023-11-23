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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.snapshots;

import java.io.File;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.classpath.ClasspathDependencyResolver;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.download.PartEnrichingContext;
import com.braintribe.devrock.mc.api.resolver.CompiledArtifactResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wirings.classpath.ClasspathResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.classpath.contract.ClasspathResolverContract;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.devrock.repolet.launcher.LauncherTrait;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.ClashResolvingStrategy;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;


public abstract class AbstractTransitiveSnapshotTest implements LauncherTrait, HasCommonFilesystemNode {
	protected static final String COM_BRAINTRIBE_DEVROCK_TEST = "com.braintribe.devrock.test";
	protected File repo;
	protected File input;
	protected File output;
	protected File initial;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/snapshots");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");
		initial = new File( input, "initial");		
	}
	
	private File settings = new File( input, "settings.xml");
	
	protected String terminal_direct = COM_BRAINTRIBE_DEVROCK_TEST + ":t#1.0.1";
	protected String terminal_ranged = COM_BRAINTRIBE_DEVROCK_TEST + ":t#1.0.1-pc";
	
	protected WireContext<ClasspathResolverContract> resolverContext;
	
	protected TransitiveResolutionContext standardTransitiveResolutionContext = TransitiveResolutionContext.build().done();
	protected ClasspathResolutionContext standardClasspathResolutionContext = ClasspathResolutionContext.build().clashResolvingStrategy(ClashResolvingStrategy.highestVersion).enrichJavadoc(true).enrichSources(true).done();
	
	protected PartEnrichingContext enrichingContext = PartEnrichingContext.build().enrichPart( PartIdentification.create("asset", "man")).done();
	protected ClasspathResolutionContext assetClasspathResolutionContext = ClasspathResolutionContext.build()
																			.clashResolvingStrategy(ClashResolvingStrategy.highestVersion)
																			.enrichJavadoc(true)
																			.enrichSources(true)
																			.enrich( enrichingContext).done();
			
	protected abstract File archiveInput();
	
	protected RepoletContent loadInput(File file) {
		if (file == null) {
			return RepoletContent.T.create();
		}
		try {
			if (file.getName().endsWith(".yaml")) {
				return RepositoryGenerations.unmarshallConfigurationFile(file);
			}
			else {
				return RepositoryGenerations.parseConfigurationFile( file);
			}
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "cannot parse file [" + file.getAbsolutePath() + "]", IllegalStateException::new);
		} 
	}	
	
	private Launcher launcher; 
	{
		launcher = Launcher.build()
				.repolet()
				.name("archive")			
					.descriptiveContent()
						.descriptiveContent( loadInput( archiveInput()))
					.close()
				.close()
			.done();
	}

	@Before
	public void runBefore() {
		TestUtils.ensure(repo);
		
		if (initial.exists()) {
			TestUtils.copy(initial, repo);
		}
		
		launcher.launch();
		
		resolverContext = Wire.contextBuilder( ClasspathResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
				.bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement(null))				
				.build();
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

	protected Pair<AnalysisArtifactResolution, Long> resolveAsDependency(String terminal, TransitiveResolutionContext resolutionContext) {
		return run(CompiledTerminal.parse(terminal), resolutionContext);
	}
	
	protected Pair<AnalysisArtifactResolution,Long> run(CompiledTerminal terminal, TransitiveResolutionContext resolutionContext) {
		try  {			
			TransitiveDependencyResolver transitiveDependencyResolver = resolverContext.contract().transitiveResolverContract().transitiveDependencyResolver();
			
			long before = System.nanoTime();
			AnalysisArtifactResolution artifactResolution = transitiveDependencyResolver.resolve( resolutionContext, terminal);
			long after = System.nanoTime();
			return Pair.of(artifactResolution, after - before);			
											
		}
		catch( Exception e) {
			e.printStackTrace();
			Assert.fail("exception thrown [" + e.getLocalizedMessage() + "]");		
		}
		return null;
	}
	
	protected Pair<AnalysisArtifactResolution, Long> resolveAsArtifact(String terminal, TransitiveResolutionContext resolutionContext) {
		try {			
			TransitiveDependencyResolver transitiveDependencyResolver = resolverContext.contract().transitiveResolverContract().transitiveDependencyResolver();			
			CompiledArtifactResolver artifactResolver = resolverContext.contract().transitiveResolverContract().dataResolverContract().directCompiledArtifactResolver();
			Maybe<CompiledArtifact> caOptional = artifactResolver.resolve( CompiledArtifactIdentification.parse( terminal));
			
			if (caOptional.isUnsatisfied()) {
				Assert.fail("no artifact found for [" + terminal + "] " + caOptional.whyUnsatisfied().asFormattedText());
				return null;
			}
					
			CompiledTerminal cdi = caOptional.get();
			long before = System.nanoTime();
			AnalysisArtifactResolution artifactResolution = transitiveDependencyResolver.resolve( resolutionContext, cdi);
			long after = System.nanoTime();
			return Pair.of(artifactResolution, after - before);					
								
		}
		catch( Exception e) {
			e.printStackTrace();
			Assert.fail("exception thrown [" + e.getLocalizedMessage() + "]");		
		}
		return null;
	}
	
	/**
	 * runs a CRP resolving with a dependency-based terminal 
	 * @param terminal - the condensed name of the dependency
	 * @param resolutionContext - the {@link ClasspathResolutionContext}
	 * @return
	 */
	protected Pair<AnalysisArtifactResolution, Long> resolve(String terminal, ClasspathResolutionContext resolutionContext) {
		try {
			ClasspathDependencyResolver classpathResolver = resolverContext.contract().classpathResolver();
			
			CompiledTerminal cdi = CompiledTerminal.from ( CompiledDependencyIdentification.parse( terminal));
			long before = System.nanoTime();
			AnalysisArtifactResolution artifactResolution = classpathResolver.resolve( resolutionContext, cdi);
			long after = System.nanoTime();
			return Pair.of(artifactResolution, after - before);					
								
		}
		catch( Exception e) {
			e.printStackTrace();
			Assert.fail("exception thrown [" + e.getLocalizedMessage() + "]");		
		}
		return null;
	}
	
	protected Pair<AnalysisArtifactResolution, Long> resolveAsArtifact(String terminal, ClasspathResolutionContext resolutionContext) {
		try {
			ClasspathDependencyResolver classpathResolver = resolverContext.contract().classpathResolver();
			
			CompiledArtifactResolver artifactResolver = resolverContext.contract().transitiveResolverContract().dataResolverContract().directCompiledArtifactResolver();
			Maybe<CompiledArtifact> caOptional = artifactResolver.resolve( CompiledArtifactIdentification.parse( terminal));
			
			if (caOptional.isUnsatisfied()) {
				Assert.fail("no artifact found for [" + terminal + "]");
				return null;
			}
					
			CompiledTerminal cdi = caOptional.get();
			long before = System.nanoTime();
			AnalysisArtifactResolution artifactResolution = classpathResolver.resolve( resolutionContext, cdi);
			long after = System.nanoTime();
			return Pair.of(artifactResolution, after - before);					
								
		}
		catch( Exception e) {
			e.printStackTrace();
			Assert.fail("exception thrown [" + e.getLocalizedMessage() + "]");		
		}
		return null;
	}
	
	
	protected void dumpResolution( AnalysisArtifactResolution resolution) {
		for (AnalysisArtifact artifact : resolution.getSolutions()) {
			System.out.println( artifact.asString());
		}
	}
	
}
