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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.offline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.classpath.ClasspathDependencyResolver;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wirings.classpath.ClasspathResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.classpath.contract.ClasspathResolverContract;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.utils.lcd.LazyInitialized;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * 
 * @author pit
 *
 */
public abstract class AbstractOfflineHandlingTest implements HasCommonFilesystemNode {

	protected File repo;
	protected File input;
	protected File output;
	
	protected LazyInitialized<YamlMarshaller> marshaller = new LazyInitialized<>( this::initMarshaller);
	protected boolean dumpResults = true;	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/offline");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	
	private File settings = new File( input, "settings.xml");
	
	protected TransitiveResolutionContext standardTransitiveResolutionContext = TransitiveResolutionContext.build().lenient( true).done();
	protected ClasspathResolutionContext standardClasspathResolutionContext = ClasspathResolutionContext.build().lenient(false).done();
			
	protected abstract RepoletContent archiveInput();	

	
	
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

	@Before
	public void runBefore() {
		TestUtils.ensure(repo); 	
		additionalSetupTask();		
	}
	
	@After
	public void runAfter() {
	}
	
	protected void launchRepolet() {
		launcher.launch();
	}
	
	protected void stopRepolet() {
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
	protected AnalysisArtifactResolution run(String terminal, ClasspathResolutionContext resolutionContext) throws Exception {
		try (				
				WireContext<ClasspathResolverContract> resolverContext = Wire.contextBuilder( ClasspathResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
					.bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement(null))				
					.build();
			) {
			
			ClasspathDependencyResolver classpathResolver = resolverContext.contract().classpathResolver();
			
			CompiledTerminal cdi = CompiledTerminal.from ( CompiledDependencyIdentification.parse( terminal));
			AnalysisArtifactResolution artifactResolution = classpathResolver.resolve( resolutionContext, cdi);
			return artifactResolution;					
								
		}			
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
		
	

	private YamlMarshaller initMarshaller() {				
		YamlMarshaller marshaller = new YamlMarshaller();
		marshaller.setWritePooled(true);		
		return new YamlMarshaller();
	}

	
	protected void dump(File file, AnalysisArtifactResolution resolution) {
		try (OutputStream out = new FileOutputStream(file)) {
			marshaller.get().marshall(out, resolution);
		}
		catch (Exception e) {
			throw Exceptions.unchecked(e, "can't dump resolution to [" + file.getAbsolutePath() + "]", IllegalStateException::new);
		}
	}
	
	/**
	 * compares two resolutions (thought to be identical)
	 * @param res1 - the first {@link AnalysisArtifactResolution}, online
	 * @param res2 - the second {@link AnalysisArtifactResolution}, offline
	 * @return - true if all went well, will throw assertion exceptions otherwise
	 */
	protected boolean compare(AnalysisArtifactResolution res1, AnalysisArtifactResolution res2) {
		if (res1.getFailure() != null) {
			Assert.assertTrue("res1 has failure, res2 has none", res2.getFailure() != null);
		}
		else {
			Assert.assertTrue("res1 has no failure, res2 has one", res2.getFailure() == null);
		}
		// terminal : must be identical 
		// solutions
		int numSolutionsRes1 = res1.getSolutions().size();
		int numSolutionsRes2 = res2.getSolutions().size();
		Assert.assertTrue( "expected res2 to have [" + numSolutionsRes1 + "] solutions, yet found [" + numSolutionsRes2 + "]", numSolutionsRes1 == numSolutionsRes2);
		
		List<String> solutionsOfRes1 = res1.getSolutions().stream().map( s -> s.asString()).collect( Collectors.toList());
		List<String> solutionsOfRes2 = res2.getSolutions().stream().map( s -> s.asString()).collect( Collectors.toList());
		List<String> matching = new ArrayList<>( solutionsOfRes1.size());
		List<String> missing = new ArrayList<>( solutionsOfRes1.size());
		
		for (String sRes1 : solutionsOfRes1) {
			if (solutionsOfRes2.contains(sRes1)) {
				matching.add( sRes1);
			}
			else {
				missing.add( sRes1);
			}
		}
		List<String> excess = new ArrayList<>( solutionsOfRes2);
		excess.removeAll( matching);
		
		// 
		Assert.assertTrue("missing in res2 [" + "]", missing.size() == 0);
		Assert.assertTrue("excess in res2 [" + "]", excess.size() == 0);
		
		return true;
	}
	
}
