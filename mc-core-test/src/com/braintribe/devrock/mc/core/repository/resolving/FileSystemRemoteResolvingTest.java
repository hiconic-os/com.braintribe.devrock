// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ============================================================================
package com.braintribe.devrock.mc.core.repository.resolving;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.devrock.mc.api.commons.VersionInfo;
import com.braintribe.devrock.mc.core.commons.utils.TestUtils;
import com.braintribe.devrock.mc.core.filters.AllMatchingArtifactFilterExpert;
import com.braintribe.devrock.mc.core.repository.local.BasicArtifactPartResolverPersistenceDelegate;
import com.braintribe.devrock.mc.core.resolver.BasicVersionInfo;
import com.braintribe.devrock.mc.core.resolver.FilesystemRepositoryArtifactDataResolver;
import com.braintribe.devrock.mc.core.resolver.LocalRepositoryCachingArtifactResolver;
import com.braintribe.devrock.model.repository.MavenHttpRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;



/**
 * tests the {@link LocalRepositoryCachingArtifactResolver} with a {@link FilesystemRepositoryArtifactDataResolver}
 * @author pit
 *
 */
public class FileSystemRemoteResolvingTest extends AbstractLocalRepositoryCachingArtifactResolverTest {
	private static final String REPOSITORY_ID = "file";
	private File resolverRepository = new File( input, "remoteRepo");
	private CompiledArtifactIdentification cai = CompiledArtifactIdentification.parse("com.braintribe.devrock.test:artifact#1.0");
	
	private Repository repository;
	{
		repository = MavenHttpRepository.T.create();
		repository.setName(REPOSITORY_ID);
	}
	
	@Before
	public void before() {
		TestUtils.ensure( repo);					
	}

	@Override
	protected String getRoot() {	
		return "filesystemArtifactPartResolving";
	}
	
	
	@Test
	public void testVersionResolving() {	
		FilesystemRepositoryArtifactDataResolver dataResolver = new FilesystemRepositoryArtifactDataResolver();
		dataResolver.setRepositoryId(REPOSITORY_ID);
		dataResolver.setRoot(resolverRepository);		
		BasicArtifactPartResolverPersistenceDelegate delegate = new BasicArtifactPartResolverPersistenceDelegate();
		delegate.setResolver(dataResolver);
		delegate.setRepository( repository);
		delegate.setArtifactFilter( AllMatchingArtifactFilterExpert.instance);
		
		LocalRepositoryCachingArtifactResolver cachingResolver = setup( Collections.singletonList( delegate));
		
		List<VersionInfo> expected = new ArrayList<>();
		expected.add( new BasicVersionInfo( cai.getVersion(), Collections.singletonList( REPOSITORY_ID)));
		
		testVersionInfoResolving( cachingResolver, cai, expected);		
	}
	
	@Test
	public void testPartResolving() {
		FilesystemRepositoryArtifactDataResolver dataResolver = new FilesystemRepositoryArtifactDataResolver();
		dataResolver.setRepositoryId(REPOSITORY_ID);
		dataResolver.setRoot(resolverRepository);		
		BasicArtifactPartResolverPersistenceDelegate delegate = new BasicArtifactPartResolverPersistenceDelegate();
		delegate.setResolver(dataResolver);
		delegate.setRepository( repository);	
		delegate.setArtifactFilter( AllMatchingArtifactFilterExpert.instance);
		LocalRepositoryCachingArtifactResolver cachingResolver = setup( Collections.singletonList( delegate));
		testPartResolving(cachingResolver, cai, standardParts);		
	}
	

}
