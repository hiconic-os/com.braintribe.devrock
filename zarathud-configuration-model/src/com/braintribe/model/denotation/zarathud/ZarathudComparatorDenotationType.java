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
package com.braintribe.model.denotation.zarathud;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface ZarathudComparatorDenotationType extends ZarathudDenotationType {

	final EntityType<ZarathudComparatorDenotationType> T = EntityTypes.T(ZarathudComparatorDenotationType.class);

	void setBaseInput( String input);
	String getBaseInput();
			
	void setBaseSolutions( String solutions);
	String getBaseSolutions();
	
	void setBaseResult( String result);
	String getBaseResult();
	
	void setBaseClassesDirectory( String baseClassesDirectory);
	String getBaseClassesDirectory();

	void setCandidateInput( String input);
	String getCandidateInput();
	
	void setCandidateSolutions( String solutions);
	String getCandidateSolutions();
	
	void setCandidateResult( String result);
	String getCandidateResult();
	
	void setCandidateClassesDirectory( String candidateClassesDirectory);
	String getCandidateClassesDirectory();
	
	void setOutput( String output);
	String getOutput();
	
	void setComparisonMode( ComparisonMode mode);
	ComparisonMode getComparisonMode();
	
}
