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
package com.braintribe.test.multi.offlineLab;

import java.io.File;

import org.junit.BeforeClass;

import com.braintribe.build.artifact.retrieval.multi.HasConnectivityTokens;

/**
 * even if the policy is tagged as "maven update always", in the second run, MC is switched into offline mode,
 * so it should find what the first run downloaded, but never the new stuff the repository is providing
 * - i.e. it should deliver the very same data as the OnlinePolicyUpdateNever run. 
 * 
 * @author pit
 *
 */
public class OfflinePolicyAlwaysLab extends AbstractOfflinePolicyLab implements HasConnectivityTokens {
	protected static File settings = new File( "res/offlineLab/contents/settings.always.xml");
	
	@BeforeClass
	public static void before() {
		before( settings);
	}
		
	@Override
	protected String[] getResultsForFirstRun() {
		return new String [] {
				"com.braintribe.test.dependencies.updatePolicyTest:A#1.0",				
				"com.braintribe.test.dependencies.updatePolicyTest:B#1.0",									
		};
	}

	@Override
	protected String[] getResultsForSecondRun() {
		return new String [] {
				"com.braintribe.test.dependencies.updatePolicyTest:A#1.0",				
				"com.braintribe.test.dependencies.updatePolicyTest:B#1.0",									
		};
	}
		
	@Override
	protected void tweakEnvironment() {
		super.tweakEnvironment();
		virtualEnvironment.addEnvironmentOverride( MC_CONNECTIVITY_MODE, MODE_OFFLINE);
	}
}
