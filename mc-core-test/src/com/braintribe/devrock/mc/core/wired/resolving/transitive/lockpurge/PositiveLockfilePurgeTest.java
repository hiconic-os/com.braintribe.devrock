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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.lockpurge;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.mc.core.commons.FilesystemLockPurger;
import com.braintribe.devrock.mc.core.wired.resolving.Collator;
import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.mc.core.wirings.maven.configuration.MavenConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.TransitiveResolverWireModule;
import com.braintribe.devrock.mc.core.wirings.transitive.contract.TransitiveResolverContract;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * tests a successful purge procedure - expected to work-out fine
 * 
 * @author pit
 *
 */
public class PositiveLockfilePurgeTest extends AbstractTransitiveRepositoryPurgerTest {
	
	private List<String> expectedLockFiles = new ArrayList<>();
	{	
		expectedLockFiles.add( "com/braintribe/devrock/test/a/1.0.1/a-1.0.1.pom.lck");
		expectedLockFiles.add( "com/braintribe/devrock/test/a/1.0.1/part-availability-third-party.txt.lck");
		expectedLockFiles.add( "com/braintribe/devrock/test/a/maven-metadata-local.xml.lck");
		expectedLockFiles.add( "com/braintribe/devrock/test/b/1.0.1/part-availability-third-party.txt.lck");
		expectedLockFiles.add( "com/braintribe/devrock/test/b/maven-metadata-local.xml.lck");
		expectedLockFiles.add( "com/braintribe/devrock/test/t/1.0.1/t-1.0.1.pom.lck");
		expectedLockFiles.add( "com/braintribe/devrock/test/t/1.0.2/part-availability-third-party.txt.lck");
		expectedLockFiles.add( "com/braintribe/devrock/test/t/maven-metadata-local.xml.lck");
	}
	
	private int numPurgedExpected = expectedLockFiles.size();

	@Override
	protected File settings() {
		return new File(input, "settings.xml");
	}

	@Override
	protected File initial() {
		return new File(input, "initial-with-lockfiles");
	}
		

	@Test
	public void purgeTest() {
		try (				
				WireContext<TransitiveResolverContract> resolverContext = Wire.contextBuilder( TransitiveResolverWireModule.INSTANCE, MavenConfigurationWireModule.INSTANCE)
					.bindContract(VirtualEnvironmentContract.class, () -> buildVirtualEnvironement(null))				
					.build();
			) {			
			FilesystemLockPurger purger =  resolverContext.contract().dataResolverContract().lockFilePurger();
			
			String localRepoPath = resolverContext.contract().dataResolverContract().repositoryReflection().getRepositoryConfiguration().cachePath();
			
			File localRepo = new File(localRepoPath);
			
			
			Maybe<Pair<Integer,List<File>>> purgeFilesytemLockFilesMaybe = purger.purgeFilesytemLockFiles();
			
			if (purgeFilesytemLockFilesMaybe.isUnsatisfied()) {
				Assert.fail( "unexpectedly unsatisfied : " + purgeFilesytemLockFilesMaybe.whyUnsatisfied().stringify());
			}
			else if (purgeFilesytemLockFilesMaybe.isIncomplete()) {
				Assert.fail( "unexpectedly incomplete : " + purgeFilesytemLockFilesMaybe.whyUnsatisfied().stringify());
			}
			else {
				// check return data 
				Pair<Integer, List<File>> response = purgeFilesytemLockFilesMaybe.get();
				int numPurged = response.first;
				List<File> cleared = response.second;
				
				Validator validator = new Validator();
				validator.assertTrue("number of purges expected [" + numPurgedExpected + "], yet found [" + numPurged +"]", numPurged == numPurgedExpected);
				
				validator.assertTrue("number of purges doesn't match, expected [" + numPurged + "], yet found [" + cleared.size() +"]", numPurged == cleared.size());
				
				List<String> convertedPurge = cleared.stream().map( f -> f.getAbsolutePath()).collect(Collectors.toList());				
				List<String> match = new ArrayList<>(convertedPurge.size());
				List<String> excess = new ArrayList<>();
				for (String purged : convertedPurge) {
					File originFile = new File( purged);
					Path relativePath = localRepo.toPath().relativize( originFile.toPath());
					String path = relativePath.toString().replace('\\', '/');
					if (expectedLockFiles.contains(path)) {
						match.add( path);
					}
					else {
						excess.add( path);
					}
				}				
				validator.assertTrue( "excess purged files :" + Collator.collateNames(excess), excess.size() == 0);
				
				List<String> missing = new ArrayList<>( expectedLockFiles);
				missing.removeAll(match);				
				validator.assertTrue( "missing purged files :" + Collator.collateNames(missing), missing.size() == 0);
												
				
				validator.assertResults();
				
			}
		}
		catch( Exception e) {
			e.printStackTrace();
			Assert.fail("exception thrown [" + e.getLocalizedMessage() + "]");		
		}

	}

}
