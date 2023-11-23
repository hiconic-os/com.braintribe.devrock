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
package com.braintribe.devrock.mc.core;

import java.io.File;

import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.api.classpath.ClasspathDependencyResolver;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.core.commons.test.HasCommonFilesystemNode;
import com.braintribe.devrock.mc.core.wirings.classpath.ClasspathResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.classpath.contract.ClasspathResolverContract;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledDependency;
import com.braintribe.model.version.Version;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

@Category(KnownIssue.class)
public class ClassifierLab implements HasCommonFilesystemNode {
	

	protected File repo;
	protected File input;
	protected File output;
	
	{	
		Pair<File,File> pair = filesystemRoots( "classifier.lab");
		input = pair.first;
		output = pair.second;
		repo = new File( output, "repo");			
	}
	
	//@Test
	public void test() {
		try (WireContext<ClasspathResolverContract> wireContext = wireContext()) {
			CompiledDependency dep = CompiledDependency.create("io.netty", "netty-transport-native-epoll", Version.parse("4.1.68.Final"), "compile", "linux-x86_64", "jar");
			ClasspathDependencyResolver classpathResolver = wireContext.contract().classpathResolver();
			
			ClasspathResolutionContext context = ClasspathResolutionContext.build().enrichJar(true).done();
			
			AnalysisArtifactResolution resolve = classpathResolver.resolve(context, dep);
		}
	}

	private  WireContext<ClasspathResolverContract> wireContext() {
		return Wire.contextBuilder(ClasspathResolverWireModule.INSTANCE)
				.bindContract(RepositoryConfigurationContract.class, () -> Maybe.complete(buildRepositoryConfiguration()))
				.build();
	}

	private  RepositoryConfiguration buildRepositoryConfiguration() {
		RepositoryConfiguration cfg = RepositoryConfiguration.T.create();
		
		cfg.setLocalRepositoryPath(repo.getAbsolutePath());
		MavenHttpRepository repo = MavenHttpRepository.T.create();
		repo.setName("third-party");
		repo.setUser("deusername");
		repo.setPassword("disissicret");
		repo.setUrl("https://artifactory.example.com/artifactory/third-party/");
		
		cfg.getRepositories().add(repo);
		
		return cfg;
	}
}
