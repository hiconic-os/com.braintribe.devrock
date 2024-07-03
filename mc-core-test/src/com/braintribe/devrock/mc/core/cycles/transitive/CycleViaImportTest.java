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
package com.braintribe.devrock.mc.core.cycles.transitive;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * tests that the correct exception is thrown 
 * @author pit
 *
 */
public class CycleViaImportTest extends AbstractTransitiveCycleTest {
	protected static final String TERMINAL = "com.braintribe.devrock.test:terminal#1.0.1";
	
	@Override
	protected File archiveContentDirectory() {
		return new File( input, "cycleViaImport");
	}

	@Test
	public void runTest() {	
		boolean exceptionThrown = false;
		boolean failureDetected = false;
		try {			
			AnalysisArtifactResolution resolution = run( TERMINAL, standardResolutionContext);
			
			if (resolution.hasFailed()) {
				failureDetected = true;
				Reason reason = resolution.getFailure();
				System.out.println( "failed : " + reason.asFormattedText());				
			}					
		}
		catch (Exception e) {			
			e.printStackTrace();
			exceptionThrown = true;
		}
		Assert.assertTrue("expected exception not thrown", exceptionThrown || failureDetected);
	}
	
}
