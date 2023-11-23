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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.reasoning;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.devrock.mc.core.wired.resolving.Validator;
import com.braintribe.devrock.model.repolet.content.RepoletContent;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisTerminal;

/**
 * tests whether the reasons of a failing terminal also shows-up in the 
 * resolution's failure.  
 * 
 * @author pit
 *
 */
public class TerminalDependencyReasoningTest extends AbstractReasoningTest {

	private static final String terminal = "com.braintribe.devrock.test:t#1.0.1";
	private List<String> expectedIncompleteArtifacts = new ArrayList<>();
	{
		expectedIncompleteArtifacts.add( terminal);
	}
	private List<String> expectedUnresolvedDependencies = new ArrayList<>();
	{
		expectedUnresolvedDependencies.add( "com.braintribe.devrock.test:missing#[1.0,1.1)/:jar");
	}

	@Override
	protected RepoletContent archiveInput() {		
		return archiveInput( "unresolved.terminal.dependency.definition.yaml");
	}
	
	@Test
	public void terminalReasoningTest() {
		try {
			AnalysisArtifactResolution resolution = run( terminal, standardTransitiveResolutionContext, true);
			AnalysisTerminal analysisTerminal = resolution.getTerminals().get(0);
			if (analysisTerminal instanceof AnalysisArtifact == false) {
				Assert.fail("unexpectedly, the terminal is not an AnalysisArtifact");
				return;
			}
			AnalysisArtifact aa = (AnalysisArtifact) analysisTerminal;

			Validator validator = new Validator();
			validator.validateFailedResolution(resolution, expectedIncompleteArtifacts, expectedUnresolvedDependencies);			

			// two different main reasons, but the causes must be identical (otherwise the transfer doesn't work properly)
			Reason resolutionFailure = resolution.getFailure();			
			Reason terminalFailure = aa.getFailure();
			validator.validateReasons( resolutionFailure.getReasons().get(0).getReasons(), terminalFailure.getReasons());
						
 
			
			validator.assertResults();
			if (dumpResults) {
				dump(new File( output, "unresolved-terminal-dependency.dump.tdr.yaml"), resolution);
			}
		} catch (Exception e) {
			Assert.fail("unexpectedly, the TDR did thrown an exception - if dependency is missing");
		}
	}

}
