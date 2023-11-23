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
package com.braintribe.devrock.model.mc.cfg.origination.resolution;

import java.util.Date;

import com.braintribe.devrock.model.mc.cfg.origination.Origination;
import com.braintribe.model.artifact.essential.ArtifactIdentification;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@SelectiveInformation("Reverse dependency lookup result created at ${date} for ${identification}")
public interface ReverseDependencyLookupResult extends Origination {
	
	EntityType<ReverseDependencyLookupResult> T = EntityTypes.T(ReverseDependencyLookupResult.class);
	
	
	String date = "date";
	String identification= "identification";

	
	// date
	Date getDate();
	void setDate(Date value);
	
	// terminal
	ArtifactIdentification getIdentification();
	void setIdentification(ArtifactIdentification value);


}
