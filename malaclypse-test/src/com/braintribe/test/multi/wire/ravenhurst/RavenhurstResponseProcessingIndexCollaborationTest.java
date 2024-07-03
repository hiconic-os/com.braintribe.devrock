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
package com.braintribe.test.multi.wire.ravenhurst;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.BasicRavenhurstRepositoryIndexPersistenceExpert;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.FilesystemBasedPersistenceExpertForRavenhurstBundle;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.RavenhurstRepositoryIndexPersistenceExpert;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.FilesystemSemaphoreLockFactory;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.registry.RavenhurstPersistenceHelper;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionImpl;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ResolvingInstant;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;
import com.braintribe.test.framework.TestUtil;
import com.braintribe.test.multi.wire.AbstractWalkerWireTest;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.wire.api.context.WireContext;



/**
 * 
 * test for the processing of RH answers. 
 * 
 * 
 * @author pit
 *
 */
@Category(KnownIssue.class)
public class RavenhurstResponseProcessingIndexCollaborationTest extends AbstractWalkerWireTest {
		
	private File contents = new File ("res/ravenhurstLab/contents/responses");
	private File localRepo = new File( contents, "repo");
	private File initalRepo = new File( contents, "initial");
	private File settings = new File( contents, "settings.index.xml");
	private FilesystemBasedPersistenceExpertForRavenhurstBundle interogationPersistenceExpert;
	private RavenhurstRepositoryIndexPersistenceExpert indexPersistenceExpert;
	
	{
		FilesystemSemaphoreLockFactory lockFactory = new FilesystemSemaphoreLockFactory();

		interogationPersistenceExpert = new FilesystemBasedPersistenceExpertForRavenhurstBundle();
		interogationPersistenceExpert.setLockFactory( lockFactory);
		
		indexPersistenceExpert = new BasicRavenhurstRepositoryIndexPersistenceExpert();
		indexPersistenceExpert.setLockFactory(lockFactory);
	}
	
	@Before
	public void before() {
		// delete the local repo
		TestUtil.ensure(localRepo);
		
		// recopy the prepared repo
		TestUtil.copy(initalRepo, localRepo);
	}

			
	private void testBundle( RavenhurstBundle bundle) {			
		WireContext<ClasspathResolverContract> context = getClasspathWalkContext( settings, localRepo, ResolvingInstant.adhoc);
		
		RepositoryReflectionImpl repositoryReflection = (RepositoryReflectionImpl) context.contract().repositoryReflection();
		repositoryReflection.purgeOutdatedMetadata();
		repositoryReflection.processRavenhurstResponse(bundle);	
	}
	
	

	/**
	 * 
	 */
	@Test
	public void testIndexAdding() {				
		try {
			
			
			File bundleFile = new File( contents, "index.collaboration.interogation");
			RavenhurstBundle ravenhurstBundle = interogationPersistenceExpert.decode(bundleFile);
			// read existing .index
			File indexfile = RavenhurstPersistenceHelper.deriveIndexFile(localRepo.getAbsolutePath(), ravenhurstBundle);
			Set<String> indexBefore = indexPersistenceExpert.decode(indexfile);			
						
			testBundle( ravenhurstBundle);
						
			// read written .index
			Set<String> indexAfter = indexPersistenceExpert.decode(indexfile);
			// passed group as in response must have been added
			Set<String> expectedGroups = ravenhurstBundle.getRavenhurstResponse().getTouchedArtifacts().stream().map( a -> {
				return a.getGroupId();
			}).collect(Collectors.toSet());
			
			for (String expected : expectedGroups) {
				if (indexBefore.contains( expected) || !indexAfter.contains( expected)) {
					Assert.fail("expected [" + expected + "] not found in index");
				}
				
			}
			
			
			
		} catch (RavenhurstException e) {			
			Assert.fail("exception thrown [" + e.getMessage());
		}
	}
	
}
