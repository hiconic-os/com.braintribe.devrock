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
package com.braintribe.devrock.mc.core.wired.resolving.transitive.snapshots;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

public class DirectSnapshotResolvingTest extends AbstractTransitiveSnapshotTest {

	@Override
	protected File archiveInput() {	
		return new File( input, "tree.definition.yaml");
	}
	
	@Test
	public void testTdrWithDirectSnapshotReference_local() {
		String terminal = COM_BRAINTRIBE_DEVROCK_TEST + ":t#1.0.3";
		
		Pair<AnalysisArtifactResolution,Long> resolveAsArtifact = resolveAsArtifact( terminal, standardTransitiveResolutionContext);
		AnalysisArtifactResolution resolution = resolveAsArtifact.first;
		if (resolution.hasFailed()) {
			Assert.fail( "failed : " + resolution.getFailure().asFormattedText());			
		}
		else {
			dumpResolution(resolution);
		}
	}
	
	@Test
	public void testTdrWithDirectSnapshotReferenceToAsset_local() {
		String terminal = COM_BRAINTRIBE_DEVROCK_TEST + ":t#1.0.5";
		
		Pair<AnalysisArtifactResolution,Long> resolveAsArtifact = resolveAsArtifact( terminal, standardTransitiveResolutionContext);
		AnalysisArtifactResolution resolution = resolveAsArtifact.first;
		if (resolution.hasFailed()) {
			Assert.fail( "failed : " + resolution.getFailure().asFormattedText());			
		}
		else {
			dumpResolution(resolution);
		}
		
	}
	
	@Test
	public void testCprWithDirectSnapshotReference_local() {
		String terminal = COM_BRAINTRIBE_DEVROCK_TEST + ":t#1.0.3";
		
		Pair<AnalysisArtifactResolution,Long> resolveAsArtifact = resolveAsArtifact( terminal, standardClasspathResolutionContext);
		AnalysisArtifactResolution resolution = resolveAsArtifact.first;
		if (resolution.hasFailed()) {
			Assert.fail( "failed : " + resolution.getFailure().asFormattedText());			
		}
		else {
			dumpResolution(resolution);
		}
		
	}
	
	@Test
	public void testTdrWithDirectSnapshotReference_remote() {
		String terminal = COM_BRAINTRIBE_DEVROCK_TEST + ":t#1.0.2";
		
		Pair<AnalysisArtifactResolution,Long> resolveAsArtifact = resolveAsArtifact( terminal, standardTransitiveResolutionContext);
		AnalysisArtifactResolution resolution = resolveAsArtifact.first;
		if (resolution.hasFailed()) {
			Assert.fail( "failed : " + resolution.getFailure().asFormattedText());			
		}
		else {
			dumpResolution(resolution);
		}	
	}
	@Test
	public void testTdrWithDirectSnapshotReferenceToAsset_remote() {
		String terminal = COM_BRAINTRIBE_DEVROCK_TEST + ":t#1.0.6";
		
		Pair<AnalysisArtifactResolution,Long> resolveAsArtifact = resolveAsArtifact( terminal, standardTransitiveResolutionContext);
		AnalysisArtifactResolution resolution = resolveAsArtifact.first;
		if (resolution.hasFailed()) {
			Assert.fail( "failed : " + resolution.getFailure().asFormattedText());			
		}
		else {
			dumpResolution(resolution);
		}
		
	}
	@Test
	public void testCprWithDirectSnapshotReference_remote() {
		String terminal = COM_BRAINTRIBE_DEVROCK_TEST + ":t#1.0.2";
		
		Pair<AnalysisArtifactResolution,Long> resolveAsArtifact = resolveAsArtifact( terminal, standardClasspathResolutionContext);
		AnalysisArtifactResolution resolution = resolveAsArtifact.first;
		if (resolution.hasFailed()) {
			Assert.fail( "failed : " + resolution.getFailure().asFormattedText());			
		}
		else {
			dumpResolution(resolution);
		}
		
	}

}
