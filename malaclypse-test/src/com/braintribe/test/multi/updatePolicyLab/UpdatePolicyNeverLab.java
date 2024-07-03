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
package com.braintribe.test.multi.updatePolicyLab;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;

import com.braintribe.test.multi.updatePolicyLab.MetaDataValidationExpectation.ValidTimestamp;

public class UpdatePolicyNeverLab extends AbstractUpdatePolicyLab {
	protected static File settings = new File( "res/updatePolicyLab/contents/settings.never.xml");	
	private CommonMetadataValidationVisitor visitor;
	
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
		
	}


	@Override
	protected CommonMetadataValidationVisitor getFirstMetadataValidationVisitor() {
		visitor = super.getFirstMetadataValidationVisitor();
		List<MetaDataValidationExpectation> expectations = new ArrayList<>();
		expectations.add( new MetaDataValidationExpectation("com.braintribe.test.dependencies.updatePolicyTest:A#1.0", ValidTimestamp.within, ValidTimestamp.within));
		expectations.add( new MetaDataValidationExpectation("com.braintribe.test.dependencies.updatePolicyTest:B#1.0", ValidTimestamp.within, ValidTimestamp.within));
		visitor.setExpectations(expectations);
		visitor.setBefore( context.beforeFirstRun);
		visitor.setAfter(context.afterFirstRun);
		visitor.setRelevantRepositoryIds( new String [] {"braintribe.Base"}); // get it from settings.xml
		return visitor;
	}


	@Override
	protected CommonMetadataValidationVisitor getSecondMetadataValidationVisitor() {
		return visitor;
	}
			
	
	
}
