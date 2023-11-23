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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.update;

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
import com.braintribe.devrock.mc.api.event.EventContext;
import com.braintribe.devrock.mc.api.transitive.TransitiveDependencyResolver;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.wirings.env.configuration.EnvironmentSensitiveConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.model.repolet.event.instance.OnDownloadEvent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.exception.Exceptions;
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
 * basic test for update issues. 
 * 
 * a) if a repository wants to be backed by RH, its update policy MUST be daily (and not NEVER!!). This
 * however posed the problem that maven-metadata.xml may be outdated (via its file time-stamp) but acutally 
 * isn't as RH already checked that (and would've marked the file as outdated) 
 * 
 * 
 * @author pit
 *
 */
//@Category(KnownIssue.class)
public abstract class AbstractMavenMetadataUpdateTest implements HasCommonFilesystemNode{
	protected final static String ARCHIVE_RH_UPDATE = "rh-archive";
	protected final static String ARCHIVE_RH_NONUPDATE = "rh-never-archive";
	protected final static String ARCHIVE_DB_UPDATE= "dumb-archive";
	protected final static String ARCHIVE_DB_NONUPDATE = "dumb-never-archive";
	
	protected File repo;
	protected File input;
	protected File output;
	protected File uploadSource;
	protected File uploadTarget;	
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/update");
		input = pair.first;
		output = pair.second;
	
		uploadSource = new File( input, "upload");
		
		repo = new File( output, "repo");
		uploadTarget = new File( output, "upload");
	
	}
		
	protected TransitiveResolutionContext standardResolutionContext = TransitiveResolutionContext.build().done();
	
	protected File initial = new File( input, "initial");	

	protected File settings = new File( input, "settings.xml");
	protected File repositoryConfiguration = new File( input, "repository-configuration.yaml");
	
	protected Map<String, List<String>> downloads;

	protected Launcher launcher;
	{
		launcher = Launcher.build()
				.repolet()
						.name(ARCHIVE_RH_UPDATE)
						.descriptiveContent()
							.descriptiveContent(archiveInput())
						.close()
						.changesUrl("http://localhost:${port}/" + ARCHIVE_RH_UPDATE + "/rest/changes")
					.close()
					.repolet()
						.name(ARCHIVE_RH_NONUPDATE)
						.descriptiveContent()
							.descriptiveContent(archiveInput())
						.close()
						.changesUrl("http://localhost:${port}/" + ARCHIVE_RH_NONUPDATE + "/rest/changes")
					.close()
					
				  .repolet()
						.name( ARCHIVE_DB_UPDATE)
						.descriptiveContent()
							.descriptiveContent(archiveInput())
						.close()						
					.close()
						
				  .repolet()
						.name(ARCHIVE_DB_NONUPDATE)
						.descriptiveContent()
							.descriptiveContent(archiveInput())
						.close()						
					.close()						
			.done();
	}
	

	protected void additionalTasks() {}
	
	protected RepoletContent archiveInput() { return RepoletContent.T.create();}
	

	@Before
	public void runBefore() {
		
		downloads = new HashMap<>();
		
		TestUtils.ensure(repo); 	
		launcher.addListener( OnDownloadEvent.T, this::onDownloadEvent);
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
		ove.setEnv( "port", Integer.toString( launcher.getAssignedPort()));				
		return ove;		
	}
	
	protected AnalysisArtifactResolution runWithMavenBasedCfg(String terminal, TransitiveResolutionContext resolutionContext) {
		final Map<String,String> overrides = new HashMap<>();
		overrides.put("ARTIFACT_REPOSITORIES_EXCLUSIVE_SETTINGS", settings.getAbsolutePath());
		
		try (				
				WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
					.bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement( overrides))				
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
	
	protected AnalysisArtifactResolution runWithRepoCfgBasedCfg(String terminal, TransitiveResolutionContext resolutionContext) {
		final Map<String,String> overrides = new HashMap<>();
		overrides.put("DEVROCK_REPOSITORY_CONFIGURATION", repositoryConfiguration.getAbsolutePath());
		try (				
				WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE,  new EnvironmentSensitiveConfigurationWireModule( buildVirtualEnvironement(overrides)))								
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

	private <E extends GenericEntity> void onDownloadEvent(EventContext eventcontext1, E event) {
		
		OnDownloadEvent edownload = (OnDownloadEvent) event;
		
		synchronized (downloads) {
			List<String> ds = downloads.computeIfAbsent( edownload.getSendingRepoletName(), k -> new ArrayList<>()); 
			ds.add( edownload.getDownloadSource());			
		}
		
	}
	
}
