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
