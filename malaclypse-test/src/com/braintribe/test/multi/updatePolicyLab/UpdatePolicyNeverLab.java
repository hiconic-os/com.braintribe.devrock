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
