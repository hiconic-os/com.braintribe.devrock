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
package com.braintribe.devrock.mc.core.wired.resolving.access;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.event.EntityEventListener;
import com.braintribe.devrock.mc.api.event.EventContext;
import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.model.repolet.event.instance.OnDownloadEvent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * tests the differening accesses during dependency resolving :
 * if a direct dependency is given, no metadata may be accessed (downloaded in this case) -> {@link DirectAccessTest}
 * if a ranged dependency is given, metadata need to be accessed -> {@link RangedAccessTest}
 * 
 * abstract test prepares a local repository with missing maven-metadata-<repo>.xml.  
 * 
 * @author pit
 *
 */
@Category(KnownIssue.class)
public abstract class AbstractAccessWhileResolvingTest implements EntityEventListener<GenericEntity>, HasCommonFilesystemNode {

	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/access");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}

	protected File initial = new File( input, "initial");
	private File settings = new File( input, "basic-settings.xml");
	
	protected static final String GRP = "com.braintribe.devrock.test"; 
	protected static final String RepositoryName = "archive";
	
	protected File expressiveContentFile = new File( input, "content.definition.txt");
	
	protected TransitiveResolutionContext standardResolutionContext = TransitiveResolutionContext.build().done();
			
	protected abstract RepoletContent archiveInput();
	
	protected RepoletContent archiveContent( File expressivefile) {
		try {
			return RepositoryGenerations.parseConfigurationFile(expressivefile);
		} catch (Exception e) {			
			e.printStackTrace();
			Assert.fail("cannot load expressive file [" + expressivefile.getAbsolutePath() + "]");
			return null;
		} 
	}
	protected Map<String,List<String>> downloadsNotified;
	
	
	private Launcher launcher; 
	{
		launcher = Launcher.build()
				.repolet()
				.name(RepositoryName)
					.descriptiveContent()
						.descriptiveContent(archiveInput())
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
		downloadsNotified = new HashMap<>();
		launcher.addListener(OnDownloadEvent.T, this::onEvent);
		launcher.launch();
	}
	
	@Override
	public void onEvent(EventContext eventContext, GenericEntity event) {
		if (event instanceof OnDownloadEvent) {
			OnDownloadEvent oevent = (OnDownloadEvent) event;
			String source = oevent.getDownloadSource();
			String name = oevent.getSendingRepoletName();
			List<String> list = downloadsNotified.computeIfAbsent( name, k -> new ArrayList<>());
			list.add( source);
						
			System.out.println("received download event from [" + name + "], source was [" + source + "]");
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
		ove.setEnv("repo", repo.getAbsolutePath());
		ove.setEnv("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settings.getAbsolutePath());
		ove.setEnv( "port", Integer.toString( launcher.getAssignedPort()));
				
		return ove;		
	}
	
	protected AnalysisArtifactResolution run(String terminal, TransitiveResolutionContext resolutionContext) {
		try (				
				WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
					.bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement(null))				
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
	
}
