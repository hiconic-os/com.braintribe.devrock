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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.invalid;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.devrock.repolet.generator.RepositoryGenerations;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;


/**
 * TDR: flags resolution correctly, yet returns 'InternalError' Reason?
 * 
 * 
 * @author pit
 *
 */
public class InvalidImportTest extends AbstractInvalidHandlingTest{

	@Override
	protected RepoletContent archiveInput() {	
		try {
			return RepositoryGenerations.unmarshallConfigurationFile( new File( input,"invalid.import.definition.yaml"));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		} 
		return null;
	}

	@Test
	public void runUnresolvedImportTestOnTDR() {
		boolean exceptionThrown = false;
		// TODO: analyze expectable reason structure
		try {
			AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#1.0.1", standardTransitiveResolutionContext);
			if (!resolution.hasFailed()) {
				Assert.fail("unexpectedly, the resolution was sucessful - even if an import is missing");
			}			
			
			System.out.println(resolution.getFailure().stringify());
			if (dumpResults) {
				dump(new File( output, "unresolved-import.dump.tdr.yaml"), resolution);
			}
			// validate reasoning here
			
		} catch (Exception e) {
			exceptionThrown = true;
		}
		Assert.assertTrue("expectedly, the TDR did throw an exception - if an import is missing", !exceptionThrown);
	}
	
	@Test
	public void runUnresolvedImportTestOnCPR() {
		boolean exceptionThrown = false;
		try {
			AnalysisArtifactResolution resolution = run("com.braintribe.devrock.test:t#1.0.1", standardClasspathResolutionContext);
			if (!resolution.hasFailed()) {
				Assert.fail("unexpectedly, the resolution was sucessful - even if an import is missing");
			}			
		} catch (Exception e) {
			e.printStackTrace();
			exceptionThrown = true;
		}
		Assert.assertTrue("unexpectedly, the CPR didn't throw an exception if an import is missing", exceptionThrown);
	}
	
}
