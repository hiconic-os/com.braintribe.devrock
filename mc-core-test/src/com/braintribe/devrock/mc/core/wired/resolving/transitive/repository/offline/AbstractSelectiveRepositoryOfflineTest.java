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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.repository.offline;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.mc.api.transitive.TransitiveResolutionContext;
import com.braintribe.devrock.mc.core.commons.ArtifactRemover;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.configuration.RepositoryConfigurationLoader;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.mc.core.wirings.env.configuration.EnvironmentSensitiveConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.model.repository.MavenFileSystemRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.devrock.repolet.launcher.Launcher;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.marshaller.artifact.maven.metadata.DeclaredMavenMetaDataMarshaller;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.artifact.maven.meta.MavenMetaData;
import com.braintribe.model.artifact.maven.meta.Versioning;
import com.braintribe.model.version.Version;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.paths.UniversalPath;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 *  
 * @author pit
 *
 *
 */
public abstract class AbstractSelectiveRepositoryOfflineTest implements HasCommonFilesystemNode {

	protected static final String COMMON_CONTEXT_DEFINITION_YAML = "archive.definition.yaml";

	protected File repoCache;
	protected File repoInstall;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots("wired/transitive/repository/offline");
		input = pair.first;
		output = pair.second;
		repoCache = new File( output, "repo");
		repoInstall = new File( output, "install");
	}
	
	protected File config() { return new File( input, "global.repository-configuration.yaml");} 
	protected File initialCache = new File( input, "local-repo");
	protected File initialRepo = new File( input, "install-repo");
	
	protected TransitiveResolutionContext standardTransitiveResolutionContext = TransitiveResolutionContext.build().lenient( true).done();
	protected ClasspathResolutionContext standardClasspathResolutionContext = ClasspathResolutionContext.build().lenient(false).done();
	
	private Launcher launcher; 
	{
		launcher = Launcher.build()
					.repolet()
					.name("archive_1")
						.descriptiveContent()
							.descriptiveContent(archiveInput())
						.close()									
					.close()
					.repolet()
					.name("archive_2")
						.descriptiveContent()
							.descriptiveContent(archiveInput())
						.close()									
					.close()					
				.done();
	}
	
	protected void additionalTasks() {}
	
	@Before
	public void runBefore() {
		
		
		// local repo -> cache
		TestUtils.ensure(repoCache);
		if (initialCache.exists()) {
			TestUtils.copy(initialCache, repoCache);
		}
		
		// install repo -> install 
		TestUtils.ensure(repoInstall);
		if (initialRepo.exists()) {
			TestUtils.copy(initialRepo, repoInstall);
		}	
		
		
		launcher.launch();		
		additionalTasks();
	}
	
	@After
	public void runAfter() {
		launcher.shutdown();
	}
	
	protected RepoletContent archiveInput() {
		return archiveInput(COMMON_CONTEXT_DEFINITION_YAML);
	};		
	
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
		ove.setEnv("cache", repoCache.getAbsolutePath());
		ove.setEnv("install", repoInstall.getAbsolutePath());
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
		
}
