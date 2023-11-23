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
package com.braintribe.test.multi.wire.ravenhurst;

import java.io.File;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.build.artifact.retrieval.multi.ravenhurst.RavenhurstException;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.FilesystemBasedPersistenceExpertForRavenhurstBundle;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.FilesystemSemaphoreLockFactory;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionImpl;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ResolvingInstant;
import com.braintribe.model.ravenhurst.interrogation.RavenhurstBundle;
import com.braintribe.test.framework.TestUtil;
import com.braintribe.test.multi.wire.AbstractWalkerWireTest;
import com.braintribe.wire.api.context.WireContext;



/**
 * 
 * test for the processing of RH answers. 
 * 
 * 
 * @author pit
 *
 */

public class RavenhurstResponseProcessingTest extends AbstractWalkerWireTest {
	
	private static final String CORE_DEV = "core-dev";
	private File contents = new File ("res/ravenhurstLab/contents/responses");
	private File localRepo = new File( contents, "repo");
	private File initalRepo = new File( contents, "initial");
	private File settings = new File( contents, "settings.xml");
	private FilesystemBasedPersistenceExpertForRavenhurstBundle persistenceExpert;
	
	{
		persistenceExpert = new FilesystemBasedPersistenceExpertForRavenhurstBundle();
		persistenceExpert.setLockFactory( new FilesystemSemaphoreLockFactory());
	}
	
	@Before
	public void before() {
		// delete the local repo
		TestUtil.ensure(localRepo);
		
		// recopy the prepared repo
		TestUtil.copy(initalRepo, localRepo);
	}

		
	
	@SuppressWarnings("unchecked")
	private void testBundle( RavenhurstBundle bundle, Consumer<File> ... validators) {			
		WireContext<ClasspathResolverContract> context = getClasspathWalkContext( settings, localRepo, ResolvingInstant.adhoc);
		
		RepositoryReflectionImpl repositoryReflection = (RepositoryReflectionImpl) context.contract().repositoryReflection();
		repositoryReflection.purgeOutdatedMetadata();
		repositoryReflection.processRavenhurstResponse(bundle);
		
		// validate
		if (validators != null) {
			for (Consumer<File> validator : validators)
			validator.accept( localRepo);
		}
	}
	
	/**
	 * test for unversioned artifact: no maven-metadata.xml of the bundle's id 
	 * @param unversionedArtifact - the folder of the unversioned artifact
	 * @param repoId - the name (id) of the repository 
	 * @return - true if test passed, false otherwise 
	 */
	private boolean testUnversioned( File unversionedArtifact, String repoId, boolean exists) {
		// unversioned : no core-dev metadata 
		File unversionedMetadataFile = new File (unversionedArtifact, "maven-metadata-" + repoId + ".xml");
		boolean condition;
		if (!exists) {
			condition = !unversionedMetadataFile.exists();
			Assert.assertTrue( "maven metadata file ["+ unversionedMetadataFile.getAbsolutePath() + "] still exists!", condition);
		}
		else {
			condition = unversionedMetadataFile.exists();
			Assert.assertTrue( "maven metadata file ["+ unversionedMetadataFile.getAbsolutePath() + "] doesn't exist!", condition);
		}
		return condition;		
	}
	
	/**
	 * test for the versioned artifact: no maven-metadata.xml of the bundle's id, not index file (.solution)
	 * @param versionedArtifact - the folder of the versioned artifact 
	 * @param repoId - the name (id) of the repository 
	 * @return - true if test passed, false otherwise 
	 */
	private boolean testVersioned( File versionedArtifact, String repoId, boolean testExists) {
		// versioned: no core-dev.solution, no core-dev metadata		
		File versionedMetadataFile = new File (versionedArtifact, "maven-metadata-" + repoId + ".xml");
		boolean conditionMaven;
		if (!testExists) {
			conditionMaven = !versionedMetadataFile.exists();
			Assert.assertTrue( "maven metadata file ["+ versionedMetadataFile.getAbsolutePath() + "] still exists!", conditionMaven);
		}
		else {
			conditionMaven = versionedMetadataFile.exists();
			Assert.assertTrue( "maven metadata file ["+ versionedMetadataFile.getAbsolutePath() + "] doesn't exist!", conditionMaven);
		}
		
		File versionedIndexFile = new File( versionedArtifact, repoId+".solution");
		boolean conditionIndex; 
		if (!testExists) {
			conditionIndex = !versionedIndexFile.exists();
			Assert.assertTrue( "index file ["+ versionedIndexFile.getAbsolutePath() + "] still exists!", conditionIndex);
		}
		else {
			conditionIndex = versionedIndexFile.exists();
			Assert.assertTrue( "index file ["+ versionedIndexFile.getAbsolutePath() + "] doesn't exist!", conditionIndex);
		}
		
		return conditionMaven && conditionIndex;		
	}
	
	
	/**
	 * a test with several versions of the same artifact to make sure that the parallel processing doesn't block<br/>
	 * versions 1.0.9, 1.0.10
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testMultipleArtifacts() {
		Consumer<File> validator = new Consumer<File>() {

			@Override
			public void accept(File t) {
				
				File unversionedArtifact = new File( t, "com/braintribe/htmltools");
				testUnversioned(unversionedArtifact, CORE_DEV, false);
				
				File versionedArtifact_zeroNine = new File( unversionedArtifact, "html-tools/1.0.9");
				testVersioned(versionedArtifact_zeroNine, CORE_DEV, false);
				
				File versionedArtifact_ten = new File( unversionedArtifact, "html-tools/1.0.10");
				testVersioned(versionedArtifact_ten, CORE_DEV, false);
			}			
		};
		
		try {
			File bundleFile = new File( contents, "multiple.interogation");
			RavenhurstBundle ravenhurstBundle = persistenceExpert.decode(bundleFile);
			testBundle( ravenhurstBundle, validator);
		} catch (RavenhurstException e) {			
			Assert.fail("exception thrown [" + e.getMessage());
		}
	}

	/**
	 * a test with several versions of the same artifact to make sure that the parallel processing doesn't block<br/>
	 * versions 1.0.10, 1.0.11 (where 1.0.11 doesn't exist)
	 */
	@Test
	public void testMixedArtifact() {
		Consumer<File> validator = new Consumer<File>() {

			@Override
			public void accept(File t) {
				// must be gone 
				File unversionedArtifact = new File( t, "com/braintribe/htmltools");
				testUnversioned(unversionedArtifact, CORE_DEV, false);

				// must be present 
				File versionedArtifact_zeroNine = new File( unversionedArtifact, "html-tools/1.0.9");
				testVersioned(versionedArtifact_zeroNine, CORE_DEV, true);
				
				// must be gone
				File versionedArtifact_ten = new File( unversionedArtifact, "html-tools/1.0.10");
				testVersioned(versionedArtifact_ten, CORE_DEV, false);
			}			
		};
		
		try {
			File bundleFile = new File( contents, "mixed.interogation");
			RavenhurstBundle ravenhurstBundle = persistenceExpert.decode(bundleFile);
			testBundle( ravenhurstBundle, validator);
		} catch (RavenhurstException e) {			
			Assert.fail("exception thrown [" + e.getMessage());
		}
	}
	
}
