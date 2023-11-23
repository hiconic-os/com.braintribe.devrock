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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.performance;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.classpath.ClasspathDependencyResolver;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.resolver.CompiledArtifactResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wirings.classpath.ClasspathResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.classpath.contract.ClasspathResolverContract;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.consumable.Part;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * abstract base class for performance tests - not to be run by CI 
 * @author pit
 *
 */
 @Category(KnownIssue.class)
public abstract class AbstractClasspathResolvingPerformanceTest implements HasCommonFilesystemNode {

	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/performance");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	
	protected File standardSettings = new File( input, "settings.xml");
	protected File adxSettings = new File( input, "settings.adx.xml");
	
	protected String getSettingsPath() {
		return standardSettings.getAbsolutePath();
	}
	
	protected TransitiveResolutionContext standardResolutionContext = TransitiveResolutionContext.build().done();
			
	protected WireContext<ClasspathResolverContract> resolverContext;
	

	@Before
	public void runBefore() throws IOException {
		
		File logConfig = new File(input, "logger.properties");
		if (logConfig.exists()) {		
			LogManager manager = LogManager.getLogManager();
	
			try (InputStream in = new FileInputStream(logConfig)) {
				manager.readConfiguration(in);
			}
		}
		
		TestUtils.ensure(repo);
		
		resolverContext = Wire.contextBuilder( ClasspathResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
				.bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement(null))				
				.build();
	}
	
	@After
	public void runAfter() {
		resolverContext.shutdown();
	}
	
	protected void reinitialize() {
		resolverContext.shutdown();
		resolverContext = Wire.contextBuilder( ClasspathResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
				.bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement(null))				
				.build();
	}
	
	protected OverridingEnvironment buildVirtualEnvironement(Map<String,String> overrides) {
		OverridingEnvironment ove = new OverridingEnvironment(StandardEnvironment.INSTANCE);
		if (overrides != null && !overrides.isEmpty()) {
			ove.setEnvs(overrides);						
		}
		ove.setEnv("M2_REPO", repo.getAbsolutePath());
		ove.setEnv("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", getSettingsPath());
				
		return ove;		
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
	
	protected Pair<AnalysisArtifactResolution, Long> resolve(String terminal, TransitiveResolutionContext resolutionContext) {
		try {
			TransitiveDependencyResolver transitiveDependencyResolver = resolverContext.contract().transitiveResolverContract().transitiveDependencyResolver();
			
			CompiledTerminal cdi = CompiledTerminal.from ( CompiledDependencyIdentification.parse( terminal));
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
	
	protected Pair<AnalysisArtifactResolution, Long> resolveAsArtifact(String terminal, TransitiveResolutionContext resolutionContext) {
		try {
			TransitiveDependencyResolver transitiveDependencyResolver = resolverContext.contract().transitiveResolverContract().transitiveDependencyResolver();			
			CompiledArtifactResolver artifactResolver = resolverContext.contract().transitiveResolverContract().dataResolverContract().directCompiledArtifactResolver();
			Maybe<CompiledArtifact> caOptional = artifactResolver.resolve( CompiledArtifactIdentification.parse( terminal));
			
			if (caOptional.isUnsatisfied()) {
				Assert.fail("no artifact found for [" + terminal + "]");
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
	
	
	protected Stream<Part> getCpJarParts(AnalysisArtifact artifact) {
		return artifact.getParts().entrySet().stream().filter(e -> e.getKey().endsWith(":jar")).map(Map.Entry::getValue);
	}
	
	protected CompiledArtifact resolve( String artifact) {
		Maybe<CompiledArtifactIdentification> caiOptional = resolverContext.contract().transitiveResolverContract().dataResolverContract().dependencyResolver().resolveDependency( CompiledDependencyIdentification.parse( artifact));
		if (caiOptional.isSatisfied()) {
			Maybe<CompiledArtifact> caOptional = resolverContext.contract().transitiveResolverContract().dataResolverContract().directCompiledArtifactResolver().resolve( caiOptional.get());
			if (caOptional.isSatisfied()) {
				return caOptional.get();
			}
			throw new IllegalStateException("cannot compile [" + caiOptional.get().asString() + "]");
		}
		
		throw new IllegalStateException("cannot resolve [" + artifact + "]");
	}

	
}
