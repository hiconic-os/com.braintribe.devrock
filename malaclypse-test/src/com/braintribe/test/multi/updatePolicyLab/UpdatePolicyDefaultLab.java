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
import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;

import com.braintribe.model.artifact.Identification;
import com.braintribe.test.multi.updatePolicyLab.MetaDataValidationExpectation.ValidTimestamp;

public class UpdatePolicyDefaultLab extends AbstractUpdatePolicyLab {
	protected static File settings = new File( "res/updatePolicyLab/contents/settings.default.xml");	
	
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
				"com.braintribe.test.dependencies.updatePolicyTest:A#1.1",				
				"com.braintribe.test.dependencies.updatePolicyTest:B#1.1",									
		};
	}

	@Override
	protected void tweakEnvironment() {
		// move date a day 
		Date date = new Date();
		date.setTime( date.getTime() - (24 * 60 * 60 * 1000));
		
		Identification a = Identification.T.create();
		a.setGroupId("com.braintribe.test.dependencies.updatePolicyTest");
		a.setArtifactId( "A");
		touchUpdateData( a, "braintribe.Base", date);
		
		Identification b = Identification.T.create();
		b.setGroupId("com.braintribe.test.dependencies.updatePolicyTest");
		b.setArtifactId( "B");
		touchUpdateData( b, "braintribe.Base", date);				
	}
		
	@Override
	protected CommonMetadataValidationVisitor getFirstMetadataValidationVisitor() {
		CommonMetadataValidationVisitor visitor = super.getFirstMetadataValidationVisitor();
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
		CommonMetadataValidationVisitor visitor = super.getFirstMetadataValidationVisitor();
		List<MetaDataValidationExpectation> expectations = new ArrayList<>();
		expectations.add( new MetaDataValidationExpectation("com.braintribe.test.dependencies.updatePolicyTest:A#1.0", ValidTimestamp.before, ValidTimestamp.within));
		expectations.add( new MetaDataValidationExpectation("com.braintribe.test.dependencies.updatePolicyTest:B#1.0", ValidTimestamp.before, ValidTimestamp.within));
		expectations.add( new MetaDataValidationExpectation("com.braintribe.test.dependencies.updatePolicyTest:A#1.1", ValidTimestamp.within, ValidTimestamp.within));
		expectations.add( new MetaDataValidationExpectation("com.braintribe.test.dependencies.updatePolicyTest:B#1.1", ValidTimestamp.within, ValidTimestamp.within));
		
		visitor.setExpectations(expectations);
		visitor.setBefore( context.beforeSecondRun);
		visitor.setAfter(context.afterSecondRun);
		visitor.setRelevantRepositoryIds( new String [] {"braintribe.Base"}); // get it from settings.xml
		return visitor;
	}
	
}
